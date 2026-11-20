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
  val sourceUrl: String
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
            organization = "Confidential emotional support",
            phone = "+8809612119911",
            available = "See provider page for availability",
            sourceUrl = "https://www.facebook.com/kaan.pete.roi/"
          ),
          HelplineContact(
            id = "hl_moner_bondhu",
            name = "Moner Bondhu (মনের বন্ধু)",
            organization = "Mental health support and counseling inquiries",
            phone = "+8801776632344",
            available = "24/7 support",
            sourceUrl = "https://www.monerbondhu.com/"
          ),
          HelplineContact(
            id = "hl_gov_mental",
            name = "National Institute of Mental Health",
            organization = "Government mental health hospital in Dhaka",
            phone = "+8801340337733",
            available = "Ask NIMH for department hours",
            sourceUrl = "https://nimh.gov.bd/contact_en.php"
          ),
          HelplineContact(
            id = "hl_women_children",
            name = "Women and children’s support",
            organization = "Bangladesh national helpline for violence against women and children",
            phone = "109",
            available = "National service",
            sourceUrl = "https://www.police.gov.bd/en/hot_line_number"
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
    _uiState.update { it.copy(isBreathingActive = true, feedbackMessage = "Breathe gently. Pause the exercise if you feel uncomfortable.") }
    breathingJob?.cancel()
    breathingJob = viewModelScope.launch {
      while (isActive) {
        // Inhale 4s
        _uiState.update { it.copy(breathingPhase = "Inhale gently (শ্বাস নিন)", breathingProgress = 1f) }
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
