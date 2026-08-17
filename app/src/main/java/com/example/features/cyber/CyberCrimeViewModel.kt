package com.example.features.cyber

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CyberResource(
  val id: String,
  val organization: String,
  val organizationBengali: String,
  val category: String, // "Cyber Harassment", "Blackmail & Doxing", "Fake Account / Impersonation"
  val hotline: String,
  val email: String,
  val operatingHours: String,
  val guideSteps: List<String>
)

data class CyberCrimeUiState(
  val resources: List<CyberResource> = emptyList(),
  val reportedIssueType: String = "Social Media Harassment",
  val incidentNotes: String = "",
  val isEvidenceChecklistCompleted: Boolean = false,
  val feedbackMessage: String? = null
)

class CyberCrimeViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CyberCrimeUiState())
  val uiState: StateFlow<CyberCrimeUiState> = _uiState.asStateFlow()

  init {
    loadCyberResources()
  }

  private fun loadCyberResources() {
    _uiState.update {
      it.copy(
        resources = listOf(
          CyberResource(
            id = "res_police_cyber",
            organization = "Police Cyber Support for Women (PCSW)",
            organizationBengali = "পুলিশ সাইবার সাপোর্ট ফর উইমেন",
            category = "Dedicated Women Cyber Desk",
            hotline = "01320000888",
            email = "cybersupport.women@police.gov.bd",
            operatingHours = "24/7 National Operations",
            guideSteps = listOf(
              "1. Take unedited full screenshots including URL, timestamps, and sender profile ID.",
              "2. Do NOT delete threatening chats or block the profile immediately before preserving URLs.",
              "3. Send digital evidence to cybersupport.women@police.gov.bd or message their official Facebook page.",
              "4. Call hotline 01320000888 for immediate case officer assignment."
            )
          ),
          CyberResource(
            id = "res_cid_cyber",
            organization = "CID Cyber Police Centre (CPC)",
            organizationBengali = "সিআইডি সাইবার পুলিশ সেন্টার",
            category = "Major Cyber Crimes & Blackmail",
            hotline = "01769691522",
            email = "cpc.cid@police.gov.bd",
            operatingHours = "24/7 Emergency Line",
            guideSteps = listOf(
              "1. Preserves cryptographic hashes of blackmailed media or fake profiles.",
              "2. Coordinates with BTRC and META/Google for rapid takedown of leaked private content.",
              "3. Physical office: Cyber Building, CID HQ, Malibagh, Dhaka."
            )
          ),
          CyberResource(
            id = "res_btrc_takedown",
            organization = "BTRC Cyber Takedown Cell",
            organizationBengali = "বিটিআরসি সাইবার সেল (১০০)",
            category = "Rapid Objectionable Content Blocking",
            hotline = "100",
            email = "btrc@btrc.gov.bd",
            operatingHours = "9:00 AM - 5:00 PM (Direct Emergency Escalation)",
            guideSteps = listOf(
              "1. Direct telecom regulatory authority for blocking unlawful URLs in Bangladesh.",
              "2. Dial 100 to report objectionable viral links or illegal harassment groups."
            )
          )
        )
      )
    }
  }

  fun updateReportedIssue(issue: String) {
    _uiState.update { it.copy(reportedIssueType = issue) }
  }

  fun updateIncidentNotes(notes: String) {
    _uiState.update { it.copy(incidentNotes = notes) }
  }

  fun submitCyberComplaintDraft() {
    _uiState.update {
      it.copy(
        feedbackMessage = "Cyber incident complaint packet prepared with timestamps! Ready to forward to PCSW."
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
