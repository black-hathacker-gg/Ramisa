package com.example.features.defense

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DefenseMove(
  val id: String,
  val title: String,
  val titleBengali: String,
  val scenario: String,
  val steps: List<String>,
  val targetAreas: String,
  val legalContext: String
)

data class SelfDefenseUiState(
  val moves: List<DefenseMove> = emptyList(),
  val selectedMove: DefenseMove? = null,
  val feedbackMessage: String? = null
)

class SelfDefenseViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(SelfDefenseUiState())
  val uiState: StateFlow<SelfDefenseUiState> = _uiState.asStateFlow()

  init {
    loadDefenseMoves()
  }

  private fun loadDefenseMoves() {
    _uiState.update {
      it.copy(
        moves = listOf(
          DefenseMove(
            id = "move_wrist",
            title = "Wrist Grab Release (কব্জি ধরা থেকে মুক্তি)",
            titleBengali = "কব্জি ছাড়ানোর কৌশল",
            scenario = "When an attacker grabs your arm or wrist from the front or behind.",
            steps = listOf(
              "1. Do not pull backwards directly against their grip strength.",
              "2. Rotate your wrist towards the attacker's thumb (the weakest point in the human grip).",
              "3. Step back, yank your arm forcefully in an upward 'V' motion towards your own shoulder.",
              "4. Immediately create distance and scream 'BACK OFF!' (পিছনে যাও!)"
            ),
            targetAreas = "Attacker's Thumb & Radial Joint",
            legalContext = "Penal Code Sec 96-106 (Right of Private Defence of Person)"
          ),
          DefenseMove(
            id = "move_palm",
            title = "Palm Heel Strike (হাতের তালুর আঘাত)",
            titleBengali = "তালুর সোজা আঘাত",
            scenario = "When an aggressor corners you closely or attempts to block your path.",
            steps = listOf(
              "1. Raise both hands to chest height in a deceptive defensive posture.",
              "2. Drive the base/heel of your dominant palm upward into the attacker's nose or chin.",
              "3. Follow through using your full body weight and hip rotation.",
              "4. Run immediately towards the nearest illuminated shop, crowd, or police post."
            ),
            targetAreas = "Nose Base, Chin, or Solar Plexus",
            legalContext = "Authorized self-defense measure to neutralize imminent bodily threat."
          ),
          DefenseMove(
            id = "move_choke",
            title = "Front Choke Defense (গলা ধরা প্রতিরোধ)",
            titleBengali = "শ্বাসরোধ প্রতিরোধ",
            scenario = "When someone attempts to grab or restrict your neck from the front.",
            steps = listOf(
              "1. Shrug your shoulders high and tuck your chin firmly into your chest to protect the windpipe.",
              "2. Raise one arm completely vertical and windmill rotate your torso to leverage their arms off.",
              "3. Drive an immediate knee strike upwards into the attacker's groin or thigh.",
              "4. Disengage and sprint while triggering 3x Power button SOS."
            ),
            targetAreas = "Groin, Knee joint, or Eyes",
            legalContext = "Critical life-saving counter-measure under Section 100 Bangladesh Penal Code."
          ),
          DefenseMove(
            id = "move_improvised",
            title = "Everyday Object Defense (দৈনন্দিন জিনিসপত্র দিয়ে আত্মরক্ষা)",
            titleBengali = "কলম, চাবি ও ব্যাগের ব্যবহার",
            scenario = "Defending yourself utilizing common carry items when unarmed.",
            steps = listOf(
              "1. Keys: Hold heavy metallic keys protruding firmly between your fingers in a fist.",
              "2. Ballpoint Pen: Grip as an ice-pick hammer strike into target zones.",
              "3. Heavy Handbag / Backpack: Swing aggressively in circular arcs to maintain safety perimeter.",
              "4. Umbrella / Water Bottle: Thrust forward into midsection to buy escape time."
            ),
            targetAreas = "Collarbone, Ribs, Thighs",
            legalContext = "Proportionate physical defence using improvised tools."
          )
        )
      )
    }
  }

  fun selectMove(move: DefenseMove?) {
    _uiState.update { it.copy(selectedMove = move) }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
