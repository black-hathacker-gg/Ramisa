package com.example.audio

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.audio.AcousticAlertGate
import com.example.core.audio.PcmWaveReader
import com.example.core.audio.YamnetModel
import com.example.domain.model.AudioInputSource
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Executes the shipped neural model on CPU; works without host mic or network. */
@RunWith(AndroidJUnit4::class)
class YamnetAudioSmokeTest {
  @Test fun bundledModelClassifiesRealSpeechAndSilenceWithAlertsDisabled() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val hash = context.assets.open(YamnetModel.MODEL_ASSET).use { input ->
      val digest = MessageDigest.getInstance("SHA-256")
      val buffer = ByteArray(8192)
      while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        digest.update(buffer, 0, count)
      }
      digest.digest().joinToString("") { "%02x".format(it) }
    }
    assertEquals("4d8b4a53282dc83ef04e3e7dbc4fbc98082e34e44ed798e16c3a0cdd4c584faf", hash)
    val samples = PcmWaveReader.readMono16Khz(context.assets.open(YamnetModel.REPLAY_ASSET))
    val gate = AcousticAlertGate()
    var bestSpeechScore = 0f
    var windows = 0
    YamnetModel(context).use { model ->
      var offset = 0
      while (offset + YamnetModel.WINDOW_SAMPLES <= samples.size) {
        val result = model.classifyPcm(samples.copyOfRange(offset, offset + YamnetModel.WINDOW_SAMPLES),
          AudioInputSource.PRERECORDED_REPLAY, offset * 1_000L / YamnetModel.SAMPLE_RATE)
        assertTrue(result.categories.size >= 500)
        assertTrue(result.categories.all { it.score.isFinite() && it.score in 0f..1f })
        bestSpeechScore = maxOf(bestSpeechScore, result.categories.first { it.label == "Speech" }.score)
        assertFalse(gate.observe(result.screamScore, result.timestampMs, result.source))
        Log.i("RamisaAudioSmoke", "window=$windows top=${result.categories.first()} rmsDbfs=${result.rmsDbfs} inferenceMs=${result.inferenceTimeMs}")
        windows++
        offset += YamnetModel.WINDOW_SAMPLES
      }
      val silence = model.classifyPcm(FloatArray(YamnetModel.WINDOW_SAMPLES), AudioInputSource.PRERECORDED_REPLAY)
      assertEquals(-120f, silence.rmsDbfs, 0.001f)
      assertTrue(silence.categories.any { it.label == "Screaming" })
    }
    assertTrue("The real speech fixture should produce at least one Speech candidate.", bestSpeechScore > 0.1f)
    assertTrue(windows > 1)
    Log.i("RamisaAudioSmoke", "PASS windows=$windows bestSpeechScore=$bestSpeechScore modelSha256=$hash")
  }
}
