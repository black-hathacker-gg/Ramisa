package com.example.features.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AreaSafetyReport
import com.example.domain.model.LiveShareSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CommunityUiState(
  val searchQuery: String = "",
  val selectedFilter: String = "ALL", // "ALL", "SAFE", "CAUTION"
  val activeLiveSession: LiveShareSession? = null,
  val isLiveSharingEnabled: Boolean = false,
  val isReportingDialogVisible: Boolean = false,
  val areaReports: List<AreaSafetyReport> = emptyList(),
  val showShareSuccessMessage: Boolean = false
)

class CommunityViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CommunityUiState())
  val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

  init {
    loadDefaultAreaReports()
  }

  private fun loadDefaultAreaReports() {
    val defaultReports = listOf(
      AreaSafetyReport(
        id = "dhk_du",
        areaName = "Dhaka University Campus (Curzon & TSC)",
        areaNameBn = "ঢাকা বিশ্ববিদ্যালয় এলাকা (কার্জন ও টিএসসি)",
        safetyScore = 91,
        lightingRating = 4.7f,
        policePatrolRating = 4.8f,
        crowdDensity = "High",
        crowdDensityBn = "উচ্চ ভিড়",
        totalReviews = 342,
        lastReportedMinutesAgo = 5,
        isCautionZone = false,
        safetyTips = listOf("Campus security active 24/7", "Well lit paths around Rokeya & Shamsun Nahar halls"),
        safetyTipsBn = listOf("২৪ ঘণ্টা ক্যাম্পাস সিকিউরিটি সক্রিয়", "রোকেয়া ও শামসুন্নাহার হলের আশপাশের রাস্তা ভালোভাবে আলোকিত")
      ),
      AreaSafetyReport(
        id = "dhk_dhanmondi",
        areaName = "Dhanmondi Lake & Satmasjid Road",
        areaNameBn = "ধানমন্ডি লেক ও সাতমসজিদ রোড",
        safetyScore = 86,
        lightingRating = 4.5f,
        policePatrolRating = 4.3f,
        crowdDensity = "High",
        crowdDensityBn = "উচ্চ ভিড়",
        totalReviews = 512,
        lastReportedMinutesAgo = 12,
        isCautionZone = false,
        safetyTips = listOf("Rabindra Sarobar has heavy police checkposts", "Main road remains active till 11 PM"),
        safetyTipsBn = listOf("রবীন্দ্র সরোবরের কাছে পুলিশি চেকপোস্ট রয়েছে", "মূল সড়ক রাত ১১টা পর্যন্ত সচল থাকে")
      ),
      AreaSafetyReport(
        id = "dhk_farmgate",
        areaName = "Farmgate Foot Overbridge & Park Road",
        areaNameBn = "ফার্মগেট ফুটওভারব্রিজ ও পার্ক রোড",
        safetyScore = 58,
        lightingRating = 2.8f,
        policePatrolRating = 3.2f,
        crowdDensity = "Moderate",
        crowdDensityBn = "মাঝারি ভিড়",
        totalReviews = 410,
        lastReportedMinutesAgo = 8,
        isCautionZone = true,
        safetyTips = listOf("Avoid dim underpass late night", "Keep belongings secure near bus counters"),
        safetyTipsBn = listOf("দেরি রাতে অন্ধকার আন্ডারপাস এড়িয়ে চলুন", "বাস কাউন্টারের পাশে ব্যাগ সাবধানে রাখুন")
      ),
      AreaSafetyReport(
        id = "dhk_gulshan",
        areaName = "Gulshan 1 & 2 Diplomatic Zone",
        areaNameBn = "গুলশান ১ ও ২ কূটনৈতিক এলাকা",
        safetyScore = 95,
        lightingRating = 4.9f,
        policePatrolRating = 5.0f,
        crowdDensity = "Moderate",
        crowdDensityBn = "মাঝারি ভিড়",
        totalReviews = 278,
        lastReportedMinutesAgo = 3,
        isCautionZone = false,
        safetyTips = listOf("Diplomatic police patrol active 24/7", "CCTV surveillance network covered"),
        safetyTipsBn = listOf("কূটনৈতিক পুলিশ সার্বক্ষণিক টহল দেয়", "পুরো এলাকা সিসিটিভি ক্যামেরার আওতাভুক্ত")
      ),
      AreaSafetyReport(
        id = "dhk_mirpur10",
        areaName = "Mirpur 10 Circle & Metro Station",
        areaNameBn = "মিরপুর ১০ গোলচত্বর ও মেট্রো স্টেশন",
        safetyScore = 84,
        lightingRating = 4.4f,
        policePatrolRating = 4.2f,
        crowdDensity = "High",
        crowdDensityBn = "উচ্চ ভিড়",
        totalReviews = 389,
        lastReportedMinutesAgo = 19,
        isCautionZone = false,
        safetyTips = listOf("Metro concourse heavily guarded", "Use main concourse exits after 9 PM"),
        safetyTipsBn = listOf("মেট্রো স্টেশন এলাকা সম্পূর্ণ সুরক্ষিত", "রাত ৯টার পর প্রধান এক্সিট ব্যবহার করুন")
      ),
      AreaSafetyReport(
        id = "dhk_uttara",
        areaName = "Uttara Sector 3 & Rabindra Sarani",
        areaNameBn = "উত্তরা সেক্টর ৩ ও রবীন্দ্র সরণি",
        safetyScore = 88,
        lightingRating = 4.6f,
        policePatrolRating = 4.4f,
        crowdDensity = "Moderate",
        crowdDensityBn = "মাঝারি ভিড়",
        totalReviews = 194,
        lastReportedMinutesAgo = 25,
        isCautionZone = false,
        safetyTips = listOf("Residential security at every gate", "Street lamps active throughout night"),
        safetyTipsBn = listOf("প্রতিটি গেটে আবাসিক নিরাপত্তা প্রহরী রয়েছে", "সারারাত স্ট্রিট লাইট জ্বলে")
      )
    )

    _uiState.update { it.copy(areaReports = defaultReports) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun setFilter(filter: String) {
    _uiState.update { it.copy(selectedFilter = filter) }
  }

  fun startLiveSharing(durationMinutes: Int = 60) {
    val code = "RMS-" + UUID.randomUUID().toString().take(6).uppercase()
    val url = "https://ramisa.live/track/$code"
    val session = LiveShareSession(
      trackingCode = code,
      shareableWebUrl = url,
      isSessionActive = true,
      expiresAtTimestamp = System.currentTimeMillis() + (durationMinutes * 60 * 1000L),
      lastKnownLatitude = 23.7258,
      lastKnownLongitude = 90.3976,
      batteryLevel = 91
    )
    _uiState.update {
      it.copy(
        activeLiveSession = session,
        isLiveSharingEnabled = true,
        showShareSuccessMessage = true
      )
    }
  }

  fun stopLiveSharing() {
    _uiState.update {
      it.copy(
        activeLiveSession = null,
        isLiveSharingEnabled = false,
        showShareSuccessMessage = false
      )
    }
  }

  fun submitCommunityReport(
    areaName: String,
    areaNameBn: String,
    safetyRating: Int,
    lightingRating: Float,
    isCaution: Boolean,
    note: String
  ) {
    val newReport = AreaSafetyReport(
      id = "custom_" + System.currentTimeMillis(),
      areaName = areaName.ifBlank { "Dhaka Local Zone" },
      areaNameBn = areaNameBn.ifBlank { "ঢাকা স্থানীয় এলাকা" },
      safetyScore = safetyRating.coerceIn(10, 100),
      lightingRating = lightingRating,
      policePatrolRating = 4.0f,
      crowdDensity = if (safetyRating > 70) "High" else "Isolated",
      crowdDensityBn = if (safetyRating > 70) "উচ্চ ভিড়" else "নির্জান",
      totalReviews = 1,
      lastReportedMinutesAgo = 0,
      isCautionZone = isCaution,
      safetyTips = if (note.isNotBlank()) listOf(note) else listOf("Community crowdsourced feedback"),
      safetyTipsBn = listOf("ব্যবহারকারীর সাম্প্রতিক নিরাপত্তা রেটিং")
    )

    _uiState.update {
      it.copy(
        areaReports = listOf(newReport) + it.areaReports,
        isReportingDialogVisible = false
      )
    }
  }

  fun toggleReportingDialog(visible: Boolean) {
    _uiState.update { it.copy(isReportingDialogVisible = visible) }
  }

  fun dismissShareMessage() {
    _uiState.update { it.copy(showShareSuccessMessage = false) }
  }
}
