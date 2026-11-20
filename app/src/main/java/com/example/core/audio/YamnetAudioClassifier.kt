package com.example.core.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.example.domain.model.AcousticCategoryScore
import com.example.domain.model.AudioInputSource
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import java.io.Closeable
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.log10
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

data class AcousticWindow(
  val categories: List<AcousticCategoryScore>,
  val rmsDbfs: Float,
  val waveform: List<Float>,
  val source: AudioInputSource,
  val timestampMs: Long,
  val inferenceTimeMs: Long
) {
  val screamScore: Float get() = categories.firstOrNull { it.label == "Screaming" }?.score ?: 0f
}

/** CPU inference over real mono PCM. The model and its labels are bundled for offline use. */
class YamnetModel(context: Context) : Closeable {
  private val classifier = AudioClassifier.createFromOptions(
    context.applicationContext,
    AudioClassifier.AudioClassifierOptions.builder()
      .setBaseOptions(
        BaseOptions.builder().setModelAssetPath(MODEL_ASSET).setDelegate(Delegate.CPU).build()
      )
      .setRunningMode(RunningMode.AUDIO_CLIPS)
      .setMaxResults(521)
      .build()
  )

  fun createRecorder(): AudioRecord = classifier.createAudioRecord(1, SAMPLE_RATE, WINDOW_SAMPLES)

  fun classifyPcm(
    samples: FloatArray,
    source: AudioInputSource,
    timestampMs: Long = SystemClock.elapsedRealtime()
  ): AcousticWindow {
    require(samples.size == WINDOW_SAMPLES) { "YAMNet input must contain one complete 975 ms window." }
    require(samples.all { it.isFinite() && it in -1f..1f }) { "PCM must be finite and normalized." }
    val audioData = AudioData.create(
      AudioData.AudioDataFormat.builder().setNumOfChannels(1).setSampleRate(SAMPLE_RATE.toFloat()).build(),
      samples.size
    )
    audioData.load(samples)
    val started = SystemClock.elapsedRealtime()
    val result = classifier.classify(audioData)
    val scores = result.classificationResults().firstOrNull()?.classifications()?.firstOrNull()
      ?.categories().orEmpty().map { AcousticCategoryScore(it.categoryName(), it.score()) }
    check(scores.isNotEmpty()) { "The audio model returned no categories." }
    check(scores.all { it.score.isFinite() && it.score in 0f..1f }) { "The audio model returned invalid scores." }
    check(scores.any { it.label == "Screaming" }) { "The bundled model does not contain the Screaming class." }
    return AcousticWindow(
      categories = scores.sortedByDescending { it.score },
      rmsDbfs = rmsDbfs(samples),
      waveform = waveform(samples),
      source = source,
      timestampMs = timestampMs,
      inferenceTimeMs = SystemClock.elapsedRealtime() - started
    )
  }

  override fun close() = classifier.close()

  companion object {
    const val MODEL_ASSET = "yamnet.tflite"
    const val SAMPLE_RATE = 16_000
    const val WINDOW_SAMPLES = 15_600
    const val REPLAY_ASSET = "yamnet_speech_16000.wav"

    fun rmsDbfs(samples: FloatArray): Float {
      require(samples.isNotEmpty())
      val rms = sqrt(samples.fold(0.0) { total, value -> total + value.toDouble() * value } / samples.size)
      return (20 * log10(rms.coerceAtLeast(0.000001))).toFloat().coerceIn(-120f, 0f)
    }

    private fun waveform(samples: FloatArray): List<Float> = List(24) { bucket ->
      val start = bucket * samples.size / 24
      val end = (bucket + 1) * samples.size / 24
      val energy = (start until end).sumOf { samples[it].toDouble() * samples[it] }
      val dbfs = 20 * log10(sqrt(energy / (end - start)).coerceAtLeast(0.000001))
      ((dbfs + 90) / 90).toFloat().coerceIn(0f, 1f)
    }
  }
}

/** One foreground-screen microphone session. Stop interrupts blocking AudioRecord reads. */
class YamnetMicrophoneMonitor(private val context: Context) {
  private val recorder = AtomicReference<AudioRecord?>(null)

  suspend fun monitor(onStarted: suspend () -> Unit, onWindow: suspend (AcousticWindow) -> Unit) =
    withContext(Dispatchers.IO) {
      check(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        "Microphone permission has not been granted."
      }
      YamnetModel(context).use { model ->
        currentCoroutineContext().ensureActive()
        val record = model.createRecorder()
        recorder.set(record)
        try {
          currentCoroutineContext().ensureActive()
          check(record.state == AudioRecord.STATE_INITIALIZED) { "Microphone initialization failed." }
          record.startRecording()
          check(record.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "Microphone did not start recording." }
          onStarted()
          val samples = FloatArray(YamnetModel.WINDOW_SAMPLES)
          while (currentCoroutineContext().isActive) {
            var filled = 0
            while (filled < samples.size && currentCoroutineContext().isActive) {
              val read = record.read(samples, filled, samples.size - filled, AudioRecord.READ_BLOCKING)
              check(read > 0) { "Microphone capture stopped or failed (code $read)." }
              filled += read
            }
            if (!currentCoroutineContext().isActive) break
            onWindow(model.classifyPcm(samples, AudioInputSource.LIVE_MICROPHONE))
          }
        } finally {
          recorder.compareAndSet(record, null)
          runCatching { record.stop() }
          record.release()
        }
      }
    }

  fun stop() {
    recorder.get()?.let { runCatching { it.stop() } }
  }
}

/** Narrow RIFF reader for the verified debug fixture; no microphone permission or network needed. */
object PcmWaveReader {
  fun readMono16Khz(input: InputStream): FloatArray {
    val bytes = input.use { it.readBytes() }
    require(bytes.size >= 44) { "Truncated WAV file." }
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    fun tag(at: Int) = String(bytes, at, 4, Charsets.US_ASCII)
    require(tag(0) == "RIFF" && tag(8) == "WAVE") { "Expected a RIFF WAV file." }
    var offset = 12
    var format = 0
    var channels = 0
    var sampleRate = 0
    var bits = 0
    var dataOffset = -1
    var dataSize = 0
    while (offset + 8 <= bytes.size) {
      val size = buffer.getInt(offset + 4)
      require(size >= 0 && size <= bytes.size - offset - 8) { "Invalid WAV chunk size." }
      when (tag(offset)) {
        "fmt " -> {
          require(size >= 16)
          format = buffer.getShort(offset + 8).toInt()
          channels = buffer.getShort(offset + 10).toInt()
          sampleRate = buffer.getInt(offset + 12)
          bits = buffer.getShort(offset + 22).toInt()
        }
        "data" -> { dataOffset = offset + 8; dataSize = size }
      }
      offset += 8 + size + size % 2
    }
    require(format == 1 && channels == 1 && sampleRate == 16_000 && bits == 16 && dataOffset >= 0 && dataSize % 2 == 0) {
      "Replay fixture must be PCM16 mono at 16 kHz."
    }
    return FloatArray(dataSize / 2) { buffer.getShort(dataOffset + it * 2) / 32768f }
  }
}
