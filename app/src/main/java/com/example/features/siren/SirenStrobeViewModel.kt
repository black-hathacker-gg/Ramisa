package com.example.features.siren

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class SirenStrobeUiState(
  val isSirenPlaying: Boolean = false,
  val isStrobeActive: Boolean = false,
  val isHighDecibelAlarm: Boolean = false,
  val sirenVolume: Float = 1.0f,
  val strobeColorIndex: Int = 0,
  val activeMode: SirenMode = SirenMode.POLICE_WHISTLE,
  val statusMessage: String? = null
)

enum class SirenMode(val title: String, val desc: String) {
  POLICE_WHISTLE("High-Pitch Whistle (হুইসেল)", "Shrill ultrasonic deterrent whistle"),
  LOUD_SIREN("Emergency Distress Siren (সাইরেন)", "Alternating high/low loud frequency"),
  DEFENSE_ALARM("Deterrent Security Alarm", "Continuous pulsating defensive alarm")
}

class SirenStrobeViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(SirenStrobeUiState())
  val uiState: StateFlow<SirenStrobeUiState> = _uiState.asStateFlow()

  private var audioJob: Job? = null
  private var strobeJob: Job? = null
  private var audioTrack: AudioTrack? = null

  fun setMode(mode: SirenMode) {
    _uiState.update { it.copy(activeMode = mode) }
    if (_uiState.value.isSirenPlaying) {
      stopSiren()
      startSiren()
    }
  }

  fun toggleSiren() {
    if (_uiState.value.isSirenPlaying) {
      stopSiren()
    } else {
      startSiren()
    }
  }

  fun toggleStrobe() {
    if (_uiState.value.isStrobeActive) {
      stopStrobe()
    } else {
      startStrobe()
    }
  }

  fun toggleAllDefense() {
    val shouldActivate = !_uiState.value.isSirenPlaying || !_uiState.value.isStrobeActive
    if (shouldActivate) {
      startSiren()
      startStrobe()
    } else {
      stopSiren()
      stopStrobe()
    }
  }

  private fun startSiren() {
    _uiState.update { it.copy(isSirenPlaying = true, statusMessage = "Audible alarm ACTIVE! Disorienting deterrent playing") }
    audioJob?.cancel()

    audioJob = viewModelScope.launch(Dispatchers.Default) {
      try {
        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_ALARM)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(minBufferSize)
          .setTransferMode(AudioTrack.MODE_STREAM)
          .build()

        audioTrack?.play()

        val buffer = ShortArray(minBufferSize)
        var phase = 0.0

        while (isActive) {
          val baseFreq = when (_uiState.value.activeMode) {
            SirenMode.POLICE_WHISTLE -> 2800.0
            SirenMode.LOUD_SIREN -> 900.0 + (500.0 * sin(phase * 0.02))
            SirenMode.DEFENSE_ALARM -> 1400.0 + (300.0 * sin(phase * 0.08))
          }

          for (i in buffer.indices) {
            val sample = sin(2.0 * PI * phase * baseFreq / sampleRate)
            buffer[i] = (sample * Short.MAX_VALUE * 0.9).toInt().toShort()
            phase += 1.0
          }

          audioTrack?.write(buffer, 0, buffer.size)
        }
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  private fun stopSiren() {
    audioJob?.cancel()
    try {
      audioTrack?.stop()
      audioTrack?.release()
      audioTrack = null
    } catch (e: Exception) {
      // Ignored
    }
    _uiState.update { it.copy(isSirenPlaying = false, statusMessage = "Audible alarm stopped") }
  }

  private fun startStrobe() {
    _uiState.update { it.copy(isStrobeActive = true) }
    strobeJob?.cancel()
    strobeJob = viewModelScope.launch {
      while (isActive) {
        delay(120) // Fast 8Hz visual disorientation strobe
        _uiState.update { it.copy(strobeColorIndex = (it.strobeColorIndex + 1) % 2) }
      }
    }
  }

  private fun stopStrobe() {
    strobeJob?.cancel()
    _uiState.update { it.copy(isStrobeActive = false, strobeColorIndex = 0) }
  }

  fun clearStatusMessage() {
    _uiState.update { it.copy(statusMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    stopSiren()
    stopStrobe()
  }
}
