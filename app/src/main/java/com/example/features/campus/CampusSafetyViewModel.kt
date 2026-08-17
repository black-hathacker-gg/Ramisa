package com.example.features.campus

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CampusHotspot(
  val id: String,
  val university: String,
  val spotName: String,
  val safetyRating: String, // "High Security", "Moderate", "Isolated at Night"
  val isWellLit: Boolean,
  val proctorOfficePhone: String,
  val nearestGuardPost: String,
  val tips: String
)

data class CampusSafetyUiState(
  val selectedUniversity: String = "University of Dhaka (DU)",
  val universities: List<String> = listOf(
    "University of Dhaka (DU)",
    "Jahangirnagar University (JU)",
    "BUET",
    "Rajshahi University (RU)",
    "Chittagong University (CU)",
    "North South University (NSU)",
    "BRAC University"
  ),
  val hotspots: List<CampusHotspot> = emptyList(),
  val emergencyDeskPhone: String = "01711-DU-SAFE",
  val proctorPhone: String = "+8801711889900",
  val feedbackMessage: String? = null
)

class CampusSafetyViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CampusSafetyUiState())
  val uiState: StateFlow<CampusSafetyUiState> = _uiState.asStateFlow()

  init {
    loadHotspotsForUniversity(_uiState.value.selectedUniversity)
  }

  fun selectUniversity(uni: String) {
    _uiState.update { it.copy(selectedUniversity = uni) }
    loadHotspotsForUniversity(uni)
  }

  private fun loadHotspotsForUniversity(uni: String) {
    val list = when (uni) {
      "University of Dhaka (DU)" -> listOf(
        CampusHotspot(
          id = "du_1",
          university = "University of Dhaka (DU)",
          spotName = "TSC to Curzon Hall Corridor",
          safetyRating = "Moderate (Crowded in Day, Isolated Post 9PM)",
          isWellLit = true,
          proctorOfficePhone = "+8801711889900",
          nearestGuardPost = "TSC Security Booth & DUMC Outpost",
          tips = "Stick to main paved road via High Court junction after 8 PM."
        ),
        CampusHotspot(
          id = "du_2",
          university = "University of Dhaka (DU)",
          spotName = "Suhrawardy Udyan Border & Mall Area",
          safetyRating = "Isolated at Night (Avoid after 7:30PM)",
          isWellLit = false,
          proctorOfficePhone = "+8801711889900",
          nearestGuardPost = "Nilkhet Gate Guard Point",
          tips = "Do not take solitary walks behind the central library after sunset."
        ),
        CampusHotspot(
          id = "du_3",
          university = "University of Dhaka (DU)",
          spotName = "Rokeya Hall to VC Square Paved Walkway",
          safetyRating = "High Security (CCTV Active)",
          isWellLit = true,
          proctorOfficePhone = "+8801711889900",
          nearestGuardPost = "Rokeya Hall Main Gate & Proctor Patrol",
          tips = "Designated female student safe corridor with active volunteer escorts."
        )
      )
      "Jahangirnagar University (JU)" -> listOf(
        CampusHotspot(
          id = "ju_1",
          university = "Jahangirnagar University (JU)",
          spotName = "Chourangi to Central Library Path",
          safetyRating = "Moderate at Night",
          isWellLit = true,
          proctorOfficePhone = "+8801711223344",
          nearestGuardPost = "Amar Ekushey Security Post",
          tips = "Stay with campus transport rickshaws after 9:00 PM."
        ),
        CampusHotspot(
          id = "ju_2",
          university = "Jahangirnagar University (JU)",
          spotName = "Tarabagh & Botanical Garden Boundary",
          safetyRating = "Isolated (Strictly Avoid at Night)",
          isWellLit = false,
          proctorOfficePhone = "+8801711223344",
          nearestGuardPost = "Main Gate Security HQ",
          tips = "Dense vegetation area. Use main road only."
        )
      )
      else -> listOf(
        CampusHotspot(
          id = "gen_1",
          university = uni,
          spotName = "Campus Main Plaza & Student Center",
          safetyRating = "High Security",
          isWellLit = true,
          proctorOfficePhone = "+8801711009911",
          nearestGuardPost = "Campus Security Desk",
          tips = "Proctor patrol vehicles present 24/7."
        ),
        CampusHotspot(
          id = "gen_2",
          university = uni,
          spotName = "Perimeter Gate & Sports Complex",
          safetyRating = "Moderate after 8 PM",
          isWellLit = true,
          proctorOfficePhone = "+8801711009911",
          nearestGuardPost = "Perimeter Booth #2",
          tips = "Use buddy system when leaving evening laboratory classes."
        )
      )
    }

    _uiState.update { it.copy(hotspots = list) }
  }

  fun requestCampusEscort(spotName: String) {
    _uiState.update {
      it.copy(feedbackMessage = "Campus volunteer & Proctor escort requested for $spotName! Patrol alerted.")
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
