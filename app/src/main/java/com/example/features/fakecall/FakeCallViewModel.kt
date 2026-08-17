package com.example.features.fakecall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FakeCallerProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FakeCallState {
  IDLE,
  COUNTDOWN,
  RINGING,
  CONNECTED,
  ENDED
}

data class FakeCallUiState(
  val state: FakeCallState = FakeCallState.IDLE,
  val selectedProfile: FakeCallerProfile = defaultProfiles.first(),
  val profiles: List<FakeCallerProfile> = defaultProfiles,
  val delaySeconds: Int = 5,
  val countdownRemaining: Int = 0,
  val callDurationSeconds: Int = 0
)

val defaultProfiles = listOf(
  FakeCallerProfile(
    id = "p1",
    name = "Abba (বাবা)",
    relation = "Father",
    phoneNumber = "+880 1711-234567",
    defaultScriptBn = "আম্মু, তুমি কোথায় এখন? আমি গাড়ির সামনে দাঁড়িয়ে আছি, তাড়াতাড়ি আসো।",
    defaultScriptEn = "Where are you now? I am waiting in the car outside, please come quickly."
  ),
  FakeCallerProfile(
    id = "p2",
    name = "Ammu (মা)",
    relation = "Mother",
    phoneNumber = "+880 1819-876543",
    defaultScriptBn = "মা, তুমি ঠিক আছো তো? রিকশার নম্বরটা আমাকে মেসেজ করে দাও তো।",
    defaultScriptEn = "Are you alright? Text me your rickshaw plate number right now."
  ),
  FakeCallerProfile(
    id = "p3",
    name = "Bhaiya (ভাইয়া)",
    relation = "Brother",
    phoneNumber = "+880 1912-334455",
    defaultScriptBn = "এই আমি মোড়ে চলে এসেছি। তুই কোন দোকানে দাঁড়িয়ে আছিস বল?",
    defaultScriptEn = "I just reached the main corner. Which spot are you standing at?"
  ),
  FakeCallerProfile(
    id = "p4",
    name = "Police OC Duty Officer",
    relation = "Dhanmondi Thana",
    phoneNumber = "01713-373126",
    defaultScriptBn = "ম্যাডাম, আপনার লোকেশন ট্র্যাকিং অ্যাক্টিভ আছে। প্যাট্রোল টিম কাছেই আছে।",
    defaultScriptEn = "Madam, your location telemetry is active. Patrol team is 100 meters away."
  )
)

class FakeCallViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(FakeCallUiState())
  val uiState: StateFlow<FakeCallUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  fun selectProfile(profile: FakeCallerProfile) {
    _uiState.update { it.copy(selectedProfile = profile) }
  }

  fun setDelaySeconds(seconds: Int) {
    _uiState.update { it.copy(delaySeconds = seconds) }
  }

  fun triggerCallSchedule() {
    val delaySecs = _uiState.value.delaySeconds
    if (delaySecs == 0) {
      _uiState.update { it.copy(state = FakeCallState.RINGING) }
      return
    }

    _uiState.update { it.copy(state = FakeCallState.COUNTDOWN, countdownRemaining = delaySecs) }
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      for (i in delaySecs downTo 1) {
        _uiState.update { it.copy(countdownRemaining = i) }
        delay(1000)
      }
      _uiState.update { it.copy(state = FakeCallState.RINGING) }
    }
  }

  fun acceptCall() {
    timerJob?.cancel()
    _uiState.update { it.copy(state = FakeCallState.CONNECTED, callDurationSeconds = 0) }
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
      }
    }
  }

  fun declineOrEndCall() {
    timerJob?.cancel()
    _uiState.update { it.copy(state = FakeCallState.IDLE, callDurationSeconds = 0, countdownRemaining = 0) }
  }

  fun cancelSchedule() {
    timerJob?.cancel()
    _uiState.update { it.copy(state = FakeCallState.IDLE, countdownRemaining = 0) }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
