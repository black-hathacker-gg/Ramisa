package com.example.features.firstaid

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FirstAidTopic(
  val id: String,
  val title: String,
  val titleBengali: String,
  val category: String, // "Trauma", "Chemical/Irritant", "Medical Emergency"
  val urgencyLevel: String, // "CRITICAL", "MODERATE", "URGENT"
  val quickAction: String,
  val steps: List<String>,
  val warnings: List<String>
)

data class FirstAidUiState(
  val topics: List<FirstAidTopic> = emptyList(),
  val searchQuery: String = "",
  val selectedTopic: FirstAidTopic? = null,
  val feedbackMessage: String? = null
)

class FirstAidViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(FirstAidUiState())
  val uiState: StateFlow<FirstAidUiState> = _uiState.asStateFlow()

  init {
    loadFirstAidTopics()
  }

  private fun loadFirstAidTopics() {
    _uiState.update {
      it.copy(
        topics = listOf(
          FirstAidTopic(
            id = "fa_pepperspray",
            title = "Pepper Spray & Tear Gas Exposure",
            titleBengali = "পিপার স্প্রে ও টিয়ার গ্যাস বিষাক্তকরণ",
            category = "Chemical/Irritant",
            urgencyLevel = "URGENT",
            quickAction = "Flush with copious cold water. Do NOT rub eyes or use oil-based lotions.",
            steps = listOf(
              "1. Move to open, well-ventilated fresh air immediately facing the wind.",
              "2. Blink rapidly to stimulate natural tears and flush residue.",
              "3. Flush eyes continuously with cold running water or sterile saline for 15 minutes.",
              "4. Wash face and skin with baby shampoo or non-oil soap.",
              "5. Remove contaminated clothing by cutting rather than pulling over the head."
            ),
            warnings = listOf(
              "Do NOT rub your eyes as this pushes capsaicin crystals deeper into the cornea.",
              "Avoid greasy oils, vaseline, or milk unless sterile saline is unavailable."
            )
          ),
          FirstAidTopic(
            id = "fa_bleeding",
            title = "Severe Bleeding & Wound Compression",
            titleBengali = "তীব্র রক্তপাত ও চাপ ব্যান্ডেজ",
            category = "Trauma",
            urgencyLevel = "CRITICAL",
            quickAction = "Apply direct, firm pressure on the wound with clean cloth without releasing.",
            steps = listOf(
              "1. Press firmly against the bleeding site with a clean cloth or sterile gauze.",
              "2. Maintain continuous direct pressure for at least 10 unbroken minutes.",
              "3. If blood soaks through, place another cloth on top—do NOT remove the initial layer.",
              "4. Elevate the injured limb above heart level if no bone fractures are suspected.",
              "5. For catastrophic limb hemorrhage, apply a tourniquet 2-3 inches above the wound."
            ),
            warnings = listOf(
              "Do NOT remove deeply impaled objects (knives, glass); stabilize them in place with rolled padding.",
              "Never remove the original dressing once clotted."
            )
          ),
          FirstAidTopic(
            id = "fa_panic_shock",
            title = "Acute Panic Attack & Hyperventilation",
            titleBengali = "তীব্র প্যানিক অ্যাটাক ও শ্বাসকষ্ট",
            category = "Medical Emergency",
            urgencyLevel = "MODERATE",
            quickAction = "Slow down breathing using 4-4-4 rhythm and guide attention outward.",
            steps = listOf(
              "1. Guide the person to sit with feet flat on the floor and shoulders relaxed.",
              "2. Inhale through nose for 4 seconds, hold for 4 seconds, exhale through pursed lips for 4 seconds.",
              "3. Speak in calm, reassuring, short sentences ('You are safe right now').",
              "4. Offer sips of cold water to stimulate the vagus nerve."
            ),
            warnings = listOf(
              "Do NOT breathe into a plastic bag (risk of hypoxia).",
              "Do NOT leave the person alone during acute dissociation."
            )
          ),
          FirstAidTopic(
            id = "fa_burns",
            title = "Burns & Acid/Thermal Injuries",
            titleBengali = "পোড়া ও অ্যাসিড আঘাত",
            category = "Trauma",
            urgencyLevel = "CRITICAL",
            quickAction = "Cool under gentle running tap water for at least 20 minutes.",
            steps = listOf(
              "1. Immediately flush the area with cool running tap water for 20-30 minutes.",
              "2. Remove jewelry and constricting items near the burn before swelling begins.",
              "3. Cover loosely with clean, non-stick plastic wrap or sterile dressing.",
              "4. For chemical/acid burns, flush continuously while calling 999 for ambulance dispatch."
            ),
            warnings = listOf(
              "Do NOT apply ice, butter, toothpaste, or turmeric powder to burns.",
              "Do NOT pop blisters or remove clothing stuck to burned skin."
            )
          )
        )
      )
    }
  }

  fun selectTopic(topic: FirstAidTopic?) {
    _uiState.update { it.copy(selectedTopic = topic) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
