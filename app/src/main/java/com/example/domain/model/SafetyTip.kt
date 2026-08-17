package com.example.domain.model

enum class TipCategory {
  TRANSIT,
  STREET_SAFETY,
  LEGAL_RIGHTS,
  CYBER_SAFETY,
  FIRST_AID
}

data class SafetyTip(
  val id: String,
  val titleEn: String,
  val titleBn: String,
  val summaryEn: String,
  val summaryBn: String,
  val detailsEn: String,
  val detailsBn: String,
  val category: TipCategory,
  val legalActRef: String? = null
)
