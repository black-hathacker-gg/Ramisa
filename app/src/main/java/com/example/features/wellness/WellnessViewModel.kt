package com.example.features.wellness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HelplineContact(
  val id: String,
  val name: String,
  val organization: String,
  val phone: String,
  val available: String,
  val isFree: Boolean
)

data class WellnessUiState(
  val isBreathingActive: Boolean = false,
  val breathingPhase: String = "Inhale", // "Inhale", "Hold", "Exhale", "Rest"
  val breathingProgress: Float = 0f,
  val helplines: List<HelplineContact> = emptyList(),
  val groundingTechniqueStep: Int = 1,
  val feedbackMessage: String? = null
)

class WellnessViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(WellnessUiState())
  val uiState: StateFlow<WellnessUiState> = _uiState.asStateFlow()

  private var breathingJob: Job? = null

  init {
    loadHelplines()
  }

  private fun loadHelplines() {
    _uiState.update {
      it.copy(
        helplines = listOf(
          HelplineContact(
            id = "hl_kanpati",
            name = "Kaan Pete Roi (কান পেতে রই)",
            organization = "First Emotional Support Helpline in Bangladesh",
            phone = "+8801779554391",
            available = "3:00 PM - 3:00 AM Daily",
            isFree = false
          ),
          HelplineContact(
            id = "hl_moner_bondhu",
            name = "Moner Bondhu (মনের বন্ধু)",
            organization = "Psychosocial Support & Trauma Counseling for Women",
            phone = "+8801776632344",
            available = "24/7 Helpline",
            isFree = false
          ),
          HelplineContact(
            id = "hl_gov_mental",
            name = "National Institute of Mental Health (NIMH)",
            organization = "Government Mental Health Services (Sherebanglanagar)",
            phone = "02-9118171",
            available = "8:00 AM - 2:30 PM",
            isFree = true
          ),
          HelplineContact(
            id = "hl_brac_maya",
            name = "Maya Apa & BRAC Mental Care",
            organization = "Digital anonymous counseling support",
            phone = "109",
            available = "24/7 Free Call",
            isFree = true
          )
        )
      )
    }
  }

  fun toggleBreathing() {
    if (_uiState.value.isBreathingActive) {
      stopBreathing()
    } else {
      startBreathing()
    }
  }

  private fun startBreathing() {
    _uiState.update { it.copy(isBreathingActive = true, feedbackMessage = "Follow the soothing 4-4-4 box breathing cycle.") }
    breathingJob?.cancel()
    breathingJob = viewModelScope.launch {
      while (isActive) {
        // Inhale 4s
        _uiState.update { it.copy(breathingPhase = "Inhale Deeply (শ্বাস নিন)", breathingProgress = 1f) }
        delay(4000)

        // Hold 4s
        _uiState.update { it.copy(breathingPhase = "Hold Breath (ধরে রাখুন)", breathingProgress = 1f) }
        delay(4000)

        // Exhale 4s
        _uiState.update { it.copy(breathingPhase = "Exhale Slowly (ধীরে ছাড়ুন)", breathingProgress = 0.2f) }
        delay(4000)

        // Rest 2s
        _uiState.update { it.copy(breathingPhase = "Relax and Rest (শান্ত হন)", breathingProgress = 0.2f) }
        delay(2000)
      }
    }
  }

  private fun stopBreathing() {
    breathingJob?.cancel()
    _uiState.update { it.copy(isBreathingActive = false, breathingPhase = "Inhale", breathingProgress = 0.5f) }
  }

  fun nextGroundingStep() {
    _uiState.update {
      val next = if (it.groundingTechniqueStep >= 5) 1 else it.groundingTechniqueStep + 1
      it.copy(groundingTechniqueStep = next)
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    stopBreathing()
  }
}
