package com.example.features.legalaid

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LegalAidOrg(
  val id: String,
  val name: String,
  val nameBengali: String,
  val serviceType: String, // "Free Legal Defense", "GD & FIR Filing Assistance", "Mediation & Shelter"
  val hotline: String,
  val address: String,
  val rightsSummary: List<String>
)

data class LegalAidUiState(
  val legalOrgs: List<LegalAidOrg> = emptyList(),
  val generalDiaryDraftCategory: String = "Street Harassment / Eve Teasing",
  val incidentDate: String = "Today",
  val incidentThana: String = "Shahbagh Police Station, Dhaka",
  val gdDraftText: String? = null,
  val feedbackMessage: String? = null
)

class LegalAidViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(LegalAidUiState())
  val uiState: StateFlow<LegalAidUiState> = _uiState.asStateFlow()

  init {
    loadLegalOrgs()
  }

  private fun loadLegalOrgs() {
    _uiState.update {
      it.copy(
        legalOrgs = listOf(
          LegalAidOrg(
            id = "org_blast",
            name = "BLAST (Bangladesh Legal Aid & Services Trust)",
            nameBengali = "বাংলাদেশ লিগ্যাল এইড অ্যান্ড সার্ভিসেস ট্রাস্ট (ব্লাস্ট)",
            serviceType = "Free Legal Aid & Court Representation",
            hotline = "01715220220",
            address = "YMCA Bhaban, 1/1 Pioneer Road, Kakrail, Dhaka",
            rightsSummary = listOf(
              "• Women have the legal right to record an FIR/GD without male guardian permission.",
              "• Free legal counsel available for victims of violence under legal aid schemes.",
              "• Dedicated legal emergency helpline for immediate arrest/harassment support."
            )
          ),
          LegalAidOrg(
            id = "org_ask",
            name = "Ain o Salish Kendra (ASK)",
            nameBengali = "আইন ও সালিশ কেন্দ্র (আসক)",
            serviceType = "Emergency Legal Counseling & Safe Shelter",
            hotline = "01724415668",
            address = "2/16 Block-B, Lalmatia, Dhaka",
            rightsSummary = listOf(
              "• Legal assistance for domestic abuse, workplace harassment, and stalking.",
              "• Safe shelter referrals and mediation services for female survivors."
            )
          ),
          LegalAidOrg(
            id = "org_gov_legal_aid",
            name = "National Legal Aid Services Organization (NLASO)",
            nameBengali = "জাতীয় আইনগত সহায়তা প্রদান সংস্থা (১৬৪৩০)",
            serviceType = "Government Free Legal Service Hotline",
            hotline = "16430",
            address = "Ministry of Law, Justice and Parliamentary Affairs, Dhaka",
            rightsSummary = listOf(
              "• Dial 16430 (Toll-Free) 24/7 for government-funded legal representation.",
              "• Panel lawyers assigned at district judge courts across all 64 districts."
            )
          )
        )
      )
    }
  }

  fun updateGdCategory(cat: String) {
    _uiState.update { it.copy(generalDiaryDraftCategory = cat) }
  }

  fun updateGdThana(thana: String) {
    _uiState.update { it.copy(incidentThana = thana) }
  }

  fun generateGeneralDiaryDraft() {
    val draft = """
      APPLICATION FOR GENERAL DIARY (সাধারণ ডায়েরি / জিডি আবেদন)
      To: Officer-in-Charge, ${_uiState.value.incidentThana}
      Subject: Information regarding ${_uiState.value.generalDiaryDraftCategory} for safety record.
      
      Sir,
      I, the undersigned victim/informant, respectfully submit that on ${_uiState.value.incidentDate}, an incident of ${_uiState.value.generalDiaryDraftCategory} occurred within your jurisdiction.
      
      Details: The offender engaged in threatening/harassing conduct without consent. I request that this matter be entered into the General Diary (GD) for my personal safety and future legal proceedings under Section 509/354/294 of the Bangladesh Penal Code.
      
      Date: Generated via RAMISA Emergency Platform
      Status: Ready for Submission at Police Station
    """.trimIndent()

    _uiState.update {
      it.copy(
        gdDraftText = draft,
        feedbackMessage = "GD Application draft formatted according to Bangladesh Thana standards!"
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
