package com.example.features.threat

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.audio.AcousticAlertGate
import com.example.core.audio.AcousticWindow
import com.example.core.audio.PcmWaveReader
import com.example.core.audio.YamnetMicrophoneMonitor
import com.example.core.audio.YamnetModel
import com.example.domain.model.AudioInputSource
import com.example.domain.model.ThreatAnalysisResult
import com.example.domain.model.ThreatLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ThreatGuardUiState(
  val isMonitoring: Boolean = false,
  val isStarting: Boolean = false,
  val isReplaying: Boolean = false,
  /** Opt-in prompts only. Sending an SOS always requires a separate confirmation. */
  val isAutoSosEnabled: Boolean = false,
  val audioSamplePoints: List<Float> = List(24) { 0f },
  val analysisResult: ThreatAnalysisResult = ThreatAnalysisResult(),
  val triggeredAutoSos: Boolean = false,
  val pendingConfirmationSource: AudioInputSource? = null,
  val confirmationSecondsRemaining: Int = 0,
  val inferenceTimeMs: Long? = null,
  val statusMessage: String = "Monitoring is off. Audio stays on this device.",
  val errorMessage: String? = null
)

class ThreatGuardViewModel(application: Application) : AndroidViewModel(application) {
  private val _uiState = MutableStateFlow(ThreatGuardUiState())
  val uiState: StateFlow<ThreatGuardUiState> = _uiState.asStateFlow()
  private val gate = AcousticAlertGate()
  private var monitoringJob: Job? = null
  private var confirmationJob: Job? = null
  private var monitor: YamnetMicrophoneMonitor? = null
  private var session = 0

  fun startMonitoring() {
    val previousJob = monitoringJob
    stopMonitoring()
    val context = getApplication<Application>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      onMicrophonePermissionDenied()
      return
    }
    val token = session
    val currentMonitor = YamnetMicrophoneMonitor(context)
    monitor = currentMonitor
    _uiState.update {
      it.copy(isStarting = true, analysisResult = ThreatAnalysisResult(), errorMessage = null,
        statusMessage = "Loading the offline audio model…")
    }
    monitoringJob = viewModelScope.launch {
      try {
        // A fast Stop/Start must not acquire the microphone before the previous session released it.
        previousJob?.join()
        currentMonitor.monitor(
          onStarted = {
            withContext(Dispatchers.Main.immediate) {
              if (token == session) _uiState.update {
                it.copy(isStarting = false, isMonitoring = true, statusMessage = "Listening through the microphone. No audio is saved or uploaded.")
              }
            }
          },
          onWindow = { frame ->
            withContext(Dispatchers.Main.immediate) { if (token == session) acceptWindow(frame) }
          }
        )
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        if (token == session) showFailure(error)
      } catch (error: LinkageError) {
        if (token == session) showFailure(error)
      }
    }
  }

  fun stopMonitoring(message: String? = null) {
    session++
    monitor?.stop()
    monitor = null
    monitoringJob?.cancel()
    monitoringJob = null
    confirmationJob?.cancel()
    confirmationJob = null
    gate.resetSequence()
    _uiState.update {
      it.copy(isMonitoring = false, isStarting = false, isReplaying = false,
        pendingConfirmationSource = null, confirmationSecondsRemaining = 0,
        triggeredAutoSos = false, audioSamplePoints = List(24) { 0f },
        statusMessage = message ?: "Monitoring is off. Audio stays on this device.")
    }
  }

  fun onMicrophonePermissionDenied() {
    stopMonitoring()
    _uiState.update {
      it.copy(errorMessage = "Microphone permission is required for live audio analysis. You can enable it in Android app settings.")
    }
  }

  fun toggleAutoSos(enabled: Boolean) {
    if (!enabled) cancelConfirmation()
    gate.resetSequence()
    _uiState.update { it.copy(isAutoSosEnabled = enabled) }
  }

  /** Called only by the real recorder's session or the clearly marked replay path. */
  internal fun acceptWindow(frame: AcousticWindow) {
    val isLive = frame.source == AudioInputSource.LIVE_MICROPHONE
    if (isLive && !_uiState.value.isMonitoring) return
    val possibleScream = frame.screamScore >= 0.65f
    val result = ThreatAnalysisResult(
      threatLevel = if (possibleScream) ThreatLevel.HIGH else ThreatLevel.LOW,
      screamModelScore = frame.screamScore,
      rmsDbfs = frame.rmsDbfs,
      categories = frame.categories.take(5),
      source = frame.source,
      recommendedAction = if (possibleScream) "Possible scream detected. An acoustic prediction cannot establish whether someone is in danger."
        else "No repeated scream candidate detected. This does not establish that your surroundings are safe."
    )
    _uiState.update {
      it.copy(analysisResult = result, audioSamplePoints = frame.waveform, inferenceTimeMs = frame.inferenceTimeMs)
    }
    val repeated = if (_uiState.value.isAutoSosEnabled) gate.observe(frame.screamScore, frame.timestampMs, frame.source)
      else { gate.resetSequence(); false }
    if (repeated && _uiState.value.isAutoSosEnabled && _uiState.value.pendingConfirmationSource == null) {
      showConfirmation(AudioInputSource.LIVE_MICROPHONE)
    }
  }

  private fun showConfirmation(source: AudioInputSource) {
    confirmationJob?.cancel()
    _uiState.update { it.copy(pendingConfirmationSource = source, confirmationSecondsRemaining = 15) }
    confirmationJob = viewModelScope.launch {
      repeat(15) {
        delay(1_000)
        _uiState.update { state -> state.copy(confirmationSecondsRemaining = (state.confirmationSecondsRemaining - 1).coerceAtLeast(0)) }
      }
      _uiState.update {
        it.copy(pendingConfirmationSource = null,
          statusMessage = "Confirmation expired. No SOS was sent.")
      }
    }
  }

  fun cancelConfirmation() {
    confirmationJob?.cancel()
    confirmationJob = null
    gate.resetSequence()
    _uiState.update {
      it.copy(pendingConfirmationSource = null, confirmationSecondsRemaining = 0,
        statusMessage = "Dismissed. No SOS was sent.")
    }
  }

  /** A test/replay can never pass this origin check, even if its score is high. */
  fun confirmSos() {
    val state = _uiState.value
    val liveConfirmed = state.pendingConfirmationSource == AudioInputSource.LIVE_MICROPHONE &&
      state.isMonitoring && state.isAutoSosEnabled && state.confirmationSecondsRemaining > 0
    stopMonitoring(if (liveConfirmed) "Opening your SOS flow…" else "Preview closed. No SOS was sent.")
    if (liveConfirmed) _uiState.update { it.copy(triggeredAutoSos = true) }
  }

  fun consumeSosRequest(): Boolean {
    if (!_uiState.value.triggeredAutoSos) return false
    _uiState.update { it.copy(triggeredAutoSos = false) }
    return true
  }

  fun previewConfirmation() {
    stopMonitoring()
    _uiState.update {
      it.copy(analysisResult = ThreatAnalysisResult(source = AudioInputSource.MANUAL_PREVIEW,
        recommendedAction = "Manual interface preview. No audio was analysed and this preview cannot send an SOS."),
        errorMessage = null, inferenceTimeMs = null, statusMessage = "Manual preview — no live alerts.")
    }
    showConfirmation(AudioInputSource.MANUAL_PREVIEW)
  }

  /** Debug demonstration executes the bundled model over a verified speech WAV, with alerts disabled. */
  fun replayBundledSpeech() {
    if (!BuildConfig.DEBUG) return
    val previousJob = monitoringJob
    stopMonitoring()
    val token = session
    _uiState.update {
      it.copy(isReplaying = true, analysisResult = ThreatAnalysisResult(source = AudioInputSource.PRERECORDED_REPLAY),
        errorMessage = null, statusMessage = "Replaying bundled speech through YAMNet. Live alerts are disabled.")
    }
    monitoringJob = viewModelScope.launch {
      try {
        previousJob?.join()
        withContext(Dispatchers.IO) {
          val context = getApplication<Application>()
          val samples = PcmWaveReader.readMono16Khz(context.assets.open(YamnetModel.REPLAY_ASSET))
          check(samples.size >= YamnetModel.WINDOW_SAMPLES) { "Replay audio is too short." }
          YamnetModel(context).use { model ->
            var offset = 0
            while (offset + YamnetModel.WINDOW_SAMPLES <= samples.size) {
              val frame = model.classifyPcm(samples.copyOfRange(offset, offset + YamnetModel.WINDOW_SAMPLES),
                AudioInputSource.PRERECORDED_REPLAY, offset * 1_000L / YamnetModel.SAMPLE_RATE)
              withContext(Dispatchers.Main.immediate) { if (token == session) acceptWindow(frame) }
              offset += YamnetModel.WINDOW_SAMPLES
            }
          }
        }
        if (token == session) _uiState.update {
          it.copy(isReplaying = false, statusMessage = "Replay complete. The scores below came from the bundled speech recording. No alerts were sent.")
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        if (token == session) showFailure(error)
      } catch (error: LinkageError) {
        if (token == session) showFailure(error)
      }
    }
  }

  private fun showFailure(error: Throwable) {
    stopMonitoring("Audio analysis stopped. No generated fallback results are shown.")
    _uiState.update {
      it.copy(errorMessage = error.message ?: "Audio analysis could not start.", analysisResult = ThreatAnalysisResult(),
        inferenceTimeMs = null)
    }
  }

  override fun onCleared() {
    stopMonitoring()
    super.onCleared()
  }
}
