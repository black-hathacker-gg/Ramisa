package com.example.domain.model

enum class ThreatLevel { LOW, MEDIUM, HIGH, CRITICAL }

/** The origin is retained so test/replay results cannot enter the live alert path. */
enum class AudioInputSource { NONE, LIVE_MICROPHONE, PRERECORDED_REPLAY, MANUAL_PREVIEW }

data class AcousticCategoryScore(val label: String, val score: Float)

data class ThreatAnalysisResult(
  val threatLevel: ThreatLevel = ThreatLevel.LOW,
  /** Raw YAMNet Screaming class score, not a probability of personal danger. */
  val screamModelScore: Float? = null,
  /** RMS level relative to digital full scale; this is not calibrated sound pressure. */
  val rmsDbfs: Float? = null,
  val categories: List<AcousticCategoryScore> = emptyList(),
  val source: AudioInputSource = AudioInputSource.NONE,
  val recommendedAction: String = "Audio monitoring is off. No sound has been analysed.",
  val timestamp: Long = System.currentTimeMillis()
)
