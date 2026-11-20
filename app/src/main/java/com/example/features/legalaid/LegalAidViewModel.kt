package com.example.features.legalaid

import androidx.lifecycle.ViewModel
import com.example.core.utils.BangladeshHotlines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LegalAidOrg(
  val id: String,
  val name: String,
  val nameBengali: String,
  val serviceType: String,
  val hotline: String,
  val address: String,
  val rightsSummary: List<String>
)

data class PoliceStationContact(
  val name: String,
  val thana: String,
  val phone: String,
  val zone: String
)

data class LegalAidUiState(
  val legalOrgs: List<LegalAidOrg> = emptyList(),
  val policeHotlines: List<PoliceStationContact> = emptyList(),
  val generalDiaryDraftCategory: String = "Street Harassment / ইভটিজিং",
  val applicantName: String = "",
  val applicantPhone: String = "",
  val applicantNid: String = "",
  val incidentDate: String = "Today",
  val incidentThana: String = "Shahbagh Thana, DMP, Dhaka",
  val incidentLocationDetail: String = "Dhaka University Campus",
  val suspectDetails: String = "Unknown persons",
  val incidentDescription: String = "",
  val gdDraftText: String? = null,
  val feedbackMessage: String? = null
)

class LegalAidViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(LegalAidUiState())
  val uiState: StateFlow<LegalAidUiState> = _uiState.asStateFlow()

  init {
    loadLegalOrgs()
    loadPoliceHotlines()
  }

  private fun loadLegalOrgs() {
    val orgs = listOf(
      LegalAidOrg(
        id = "org_blast",
        name = "BLAST (Bangladesh Legal Aid & Services Trust)",
        nameBengali = "বাংলাদেশ লিগ্যাল এইড অ্যান্ড সার্ভিসেস ট্রাস্ট (ব্লাস্ট)",
        serviceType = "Free Legal Defense & Representation",
        hotline = "01715220220",
        address = "YMCA Bhaban, 1/1 Pioneer Road, Kakrail, Dhaka",
        rightsSummary = listOf(
          "Women have the legal right to record an FIR/GD without male guardian permission.",
          "Free legal defense and representation for violence and harassment survivors.",
          "24/7 dedicated legal emergency helpline for immediate assistance."
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
          "Legal counseling for domestic abuse, workplace harassment, and blackmail.",
          "Safe shelter referrals and legal mediation support for female survivors."
        )
      ),
      LegalAidOrg(
        id = "org_gov_legal_aid",
        name = "Bangladesh Legal Aid Department",
        nameBengali = "বাংলাদেশ আইনগত সহায়তা অধিদপ্তর (${BangladeshHotlines.LEGAL_AID_BANGLA})",
        serviceType = "Government Free Legal Service Hotline",
        hotline = BangladeshHotlines.LEGAL_AID,
        address = "Ministry of Law, Justice and Parliamentary Affairs, Dhaka",
        rightsSummary = listOf(
          "Call ${BangladeshHotlines.LEGAL_AID} toll-free for information about government-funded legal assistance.",
          "Government panel lawyers assigned at district judge courts across all 64 districts."
        )
      )
    )
    _uiState.update { it.copy(legalOrgs = orgs) }
  }

  private fun loadPoliceHotlines() {
    val hotlines = listOf(
      PoliceStationContact("Police Cyber Support for Women (PCSW)", "PHQ, Dhaka", "01320000888", "Cyber Helpline"),
      PoliceStationContact("CID Cyber Police Centre (CPC)", "Malibagh, Dhaka", "01730336431", "Cyber Investigation"),
      PoliceStationContact("DMP Central Control Room", "Abdul Gani Road, Dhaka", "01713373127", "Dhaka Central"),
      PoliceStationContact("Shahbagh Thana Duty Officer", "Shahbagh, Dhaka", "01713373128", "Dhaka South"),
      PoliceStationContact("Dhanmondi Thana Duty Officer", "Dhanmondi, Dhaka", "01713373130", "Dhaka South"),
      PoliceStationContact("Mirpur Model Thana Duty Officer", "Mirpur, Dhaka", "01713373133", "Dhaka North"),
      PoliceStationContact("Gulshan Thana Duty Officer", "Gulshan, Dhaka", "01713373144", "Dhaka North"),
      PoliceStationContact("Dhaka University Proctor Control", "TSC, DU", "01711822334", "Campus Security")
    )
    _uiState.update { it.copy(policeHotlines = hotlines) }
  }

  fun updateGdCategory(cat: String) {
    _uiState.update { it.copy(generalDiaryDraftCategory = cat) }
  }

  fun updateApplicantName(name: String) {
    _uiState.update { it.copy(applicantName = name) }
  }

  fun updateApplicantPhone(phone: String) {
    _uiState.update { it.copy(applicantPhone = phone) }
  }

  fun updateApplicantNid(nid: String) {
    _uiState.update { it.copy(applicantNid = nid) }
  }

  fun updateGdThana(thana: String) {
    _uiState.update { it.copy(incidentThana = thana) }
  }

  fun updateLocationDetail(loc: String) {
    _uiState.update { it.copy(incidentLocationDetail = loc) }
  }

  fun updateSuspectDetails(suspect: String) {
    _uiState.update { it.copy(suspectDetails = suspect) }
  }

  fun updateIncidentDescription(desc: String) {
    _uiState.update { it.copy(incidentDescription = desc) }
  }

  fun generateGeneralDiaryDraft() {
    val state = _uiState.value
    val name = if (state.applicantName.isNotBlank()) state.applicantName else "[Applicant Name]"
    val phone = if (state.applicantPhone.isNotBlank()) state.applicantPhone else "[Mobile Number]"
    val nid = if (state.applicantNid.isNotBlank()) state.applicantNid else "[NID / Birth Certificate ID]"
    val thana = if (state.incidentThana.isNotBlank()) state.incidentThana else "Shahbagh Thana, Dhaka"
    val loc = if (state.incidentLocationDetail.isNotBlank()) state.incidentLocationDetail else "Incident Location"
    val category = state.generalDiaryDraftCategory
    val suspect = if (state.suspectDetails.isNotBlank()) state.suspectDetails else "Unknown suspects"
    val desc = if (state.incidentDescription.isNotBlank()) {
      state.incidentDescription
    } else {
      "The accused persons stalked, harassed, and issued verbal threats without consent. I am facing severe security risk."
    }

    val lawSections = when {
      category.contains("ইভটিজিং") || category.contains("Street") -> "Penal Code 1860 (Sections 509, 354, 294)"
      category.contains("সাইবার") || category.contains("Cyber") || category.contains("Blackmail") -> "Cyber Security Act 2023 / Penal Code 506 & 509"
      category.contains("পারিবারিক") || category.contains("Domestic") -> "Domestic Violence (Prevention and Protection) Act 2010"
      else -> "Relevant Sections of Bangladesh Penal Code 1860"
    }

    val draft = StringBuilder().apply {
      appendLine("==================================================")
      appendLine("APPLICATION FOR GENERAL DIARY (সাধারণ ডায়েরি / জিডি আবেদন)")
      appendLine("==================================================")
      appendLine("To:")
      appendLine("Officer-in-Charge (OC)")
      appendLine(thana)
      appendLine()
      appendLine("Subject: Information regarding $category for entry into General Diary (GD).")
      appendLine()
      appendLine("Sir,")
      appendLine("I, the undersigned applicant, respectfully submit the following facts:")
      appendLine("1. Applicant Information:")
      appendLine("   Name: $name")
      appendLine("   Mobile: $phone")
      appendLine("   National ID: $nid")
      appendLine()
      appendLine("2. Incident Occurrence:")
      appendLine("   Date & Time: ${state.incidentDate}")
      appendLine("   Location: $loc")
      appendLine("   Accused / Suspects: $suspect")
      appendLine()
      appendLine("3. Statement of Incident:")
      appendLine("   $desc")
      appendLine()
      appendLine("In light of the above circumstances, I feel highly insecure. To preserve my legal rights, safety, and evidence under $lawSections, I humbly request you to record this matter in the General Diary (GD).")
      appendLine()
      appendLine("Attachment: Digital Proofs & Audio Log from RAMISA Secure Platform.")
      appendLine()
      appendLine("Applicant Signature: $name")
      appendLine("Date: ${state.incidentDate}")
      appendLine("--------------------------------------------------")
      appendLine("[Generated via RAMISA Women Safety & Legal Aid Engine]")
      appendLine("==================================================")
    }.toString()

    _uiState.update {
      it.copy(
        gdDraftText = draft,
        feedbackMessage = "GD Application draft formatted and ready for submission!"
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
