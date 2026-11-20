package com.example.core.audio

import com.example.domain.model.AudioInputSource

/** A heuristic on actual model output. It never represents a verified emergency. */
class AcousticAlertGate(
  private val scoreThreshold: Float = 0.65f,
  private val requiredWindows: Int = 2,
  private val maximumGapMs: Long = 2_500L,
  private val cooldownMs: Long = 30_000L
) {
  private var consecutive = 0
  private var lastWindowMs: Long? = null
  private var lastPromptMs: Long? = null

  init {
    require(scoreThreshold in 0f..1f)
    require(requiredWindows >= 2)
    require(maximumGapMs > 0 && cooldownMs >= 0)
  }

  @Synchronized
  fun observe(score: Float, timestampMs: Long, source: AudioInputSource): Boolean {
    if (source != AudioInputSource.LIVE_MICROPHONE || !score.isFinite() || score !in 0f..1f || score < scoreThreshold) {
      resetSequence()
      return false
    }
    val previous = lastWindowMs
    // A duplicate/replayed frame is not a second independent acoustic window.
    if (previous != null && timestampMs <= previous) return false
    consecutive = if (previous == null || timestampMs - previous > maximumGapMs) 1 else consecutive + 1
    lastWindowMs = timestampMs
    val lastPrompt = lastPromptMs
    if (lastPrompt != null && timestampMs - lastPrompt < cooldownMs) return false
    if (consecutive < requiredWindows) return false
    lastPromptMs = timestampMs
    resetSequence()
    return true
  }

  @Synchronized
  fun resetSequence() {
    consecutive = 0
    lastWindowMs = null
  }
}
