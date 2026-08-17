package com.example.features.threat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ThreatAnalysisResult
import com.example.domain.model.ThreatLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ThreatGuardUiState(
  val isMonitoring: Boolean = false,
  val autoSosTriggerThreshold: ThreatLevel = ThreatLevel.CRITICAL,
  val currentDecibels: Int = 38,
  val isAutoSosEnabled: Boolean = true,
  val isHotwordListeningEnabled: Boolean = true,
  val detectedHotword: String? = null,
  val audioSamplePoints: List<Float> = List(24) { 0.2f },
  val analysisResult: ThreatAnalysisResult = ThreatAnalysisResult(),
  val triggeredAutoSos: Boolean = false,
  val recentDetections: List<ThreatAnalysisResult> = emptyList()
)

class ThreatGuardViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(ThreatGuardUiState())
  val uiState: StateFlow<ThreatGuardUiState> = _uiState.asStateFlow()

  private var monitoringJob: Job? = null

  fun toggleMonitoring() {
    val current = _uiState.value.isMonitoring
    if (current) {
      stopMonitoring()
    } else {
      startMonitoring()
    }
  }

  fun toggleHotword(enabled: Boolean) {
    _uiState.update { it.copy(isHotwordListeningEnabled = enabled) }
  }

  fun triggerHotwordSOS(phrase: String) {
    val hotwordResult = ThreatAnalysisResult(
      threatLevel = ThreatLevel.CRITICAL,
      confidenceScore = 0.98f,
      decibelLevel = 84,
      detectedAnomalies = listOf("Emergency Hotword Triggered: '$phrase'", "Voice Panic Pattern Verified"),
      recommendedAction = "Voice emergency hotword '$phrase' detected. Firing instant SOS broadcast!",
      recommendedActionBn = "ভয়েস জরুরি হটওয়ার্ড '$phrase' শনাক্ত হয়েছে। তাৎক্ষণিক এসওএস পাঠানো হচ্ছে!",
      timestamp = System.currentTimeMillis()
    )

    _uiState.update {
      it.copy(
        detectedHotword = phrase,
        analysisResult = hotwordResult,
        triggeredAutoSos = true,
        recentDetections = (listOf(hotwordResult) + it.recentDetections).take(6)
      )
    }
  }

  fun startMonitoring() {
    _uiState.update { it.copy(isMonitoring = true, triggeredAutoSos = false) }
    monitoringJob?.cancel()
    monitoringJob = viewModelScope.launch {
      while (isActive) {
        delay(350)
        // Generate continuous audio waveform sample
        val baseNoise = Random.nextInt(32, 55)
        val spikeChance = Random.nextInt(100)

        val (db, threatLevel, anomalies, actionEn, actionBn) = when {
          spikeChance > 94 -> {
            val highDb = Random.nextInt(86, 105)
            Tuple5(
              highDb,
              ThreatLevel.HIGH,
              listOf("Sudden High Amplitude Spike", "Aggressive Audio Frequency Detected"),
              "Loud scream or violent impact pattern detected. Move to safe zone.",
              "উচ্চ চিৎকারের শব্দ বা সংঘর্ষ শনাক্ত হয়েছে। নিরাপদ স্থানে যান।"
            )
          }
          spikeChance > 80 -> {
            val medDb = Random.nextInt(65, 82)
            Tuple5(
              medDb,
              ThreatLevel.MEDIUM,
              listOf("Elevated Ambient Sound Level", "Crowd Disturbance / Fast Motion"),
              "Surrounding commotion detected. Keep contacts informed.",
              "আশেপাশে অস্বাভাবিক শোরগোল শনাক্ত। সতর্ক থাকুন।"
            )
          }
          else -> {
            Tuple5(
              baseNoise,
              ThreatLevel.LOW,
              emptyList(),
              "Normal background sound profile.",
              "স্বাভাবিক ও নিরাপদ পরিবেশ।"
            )
          }
        }

        val updatedWaveform = (_uiState.value.audioSamplePoints.drop(1) + (db / 110f)).takeLast(24)

        val result = ThreatAnalysisResult(
          threatLevel = threatLevel,
          confidenceScore = if (threatLevel == ThreatLevel.LOW) 0.96f else 0.89f,
          decibelLevel = db,
          detectedAnomalies = anomalies,
          recommendedAction = actionEn,
          recommendedActionBn = actionBn,
          timestamp = System.currentTimeMillis()
        )

        val autoTrigger = _uiState.value.isAutoSosEnabled && threatLevel == ThreatLevel.CRITICAL

        _uiState.update { state ->
          val newHistory = if (threatLevel != ThreatLevel.LOW) {
            (listOf(result) + state.recentDetections).take(6)
          } else {
            state.recentDetections
          }

          state.copy(
            currentDecibels = db,
            audioSamplePoints = updatedWaveform,
            analysisResult = result,
            triggeredAutoSos = autoTrigger,
            recentDetections = newHistory
          )
        }
      }
    }
  }

  fun stopMonitoring() {
    monitoringJob?.cancel()
    monitoringJob = null
    _uiState.update { it.copy(isMonitoring = false, currentDecibels = 0) }
  }

  fun toggleAutoSos(enabled: Boolean) {
    _uiState.update { it.copy(isAutoSosEnabled = enabled) }
  }

  fun simulateThreatSpike() {
    val simulated = ThreatAnalysisResult(
      threatLevel = ThreatLevel.CRITICAL,
      confidenceScore = 0.95f,
      decibelLevel = 98,
      detectedAnomalies = listOf("Acoustic Shock Spike", "High-Distress Scream Signature (Bangla/English)"),
      recommendedAction = "Severe distress signal detected! Initiating immediate SOS dispatch.",
      recommendedActionBn = "উচ্চ বিপদের সংকেত শনাক্ত হয়েছে! জরুরি এসওএস সক্রিয় করা হচ্ছে।",
      timestamp = System.currentTimeMillis()
    )

    _uiState.update {
      it.copy(
        currentDecibels = 98,
        analysisResult = simulated,
        triggeredAutoSos = it.isAutoSosEnabled,
        recentDetections = (listOf(simulated) + it.recentDetections).take(6)
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    monitoringJob?.cancel()
  }

  private data class Tuple5(
    val db: Int,
    val level: ThreatLevel,
    val anomalies: List<String>,
    val en: String,
    val bn: String
  )
}
