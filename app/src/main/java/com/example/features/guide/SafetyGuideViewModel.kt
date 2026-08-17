package com.example.features.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SafetyGuideRepository
import com.example.domain.model.SafetyTip
import com.example.domain.model.TipCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SafetyGuideUiState(
  val selectedCategory: TipCategory? = null,
  val tips: List<SafetyTip> = emptyList(),
  val expandedTipId: String? = null
)

class SafetyGuideViewModel(
  private val repository: SafetyGuideRepository = SafetyGuideRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(SafetyGuideUiState())
  val uiState: StateFlow<SafetyGuideUiState> = _uiState.asStateFlow()

  init {
    loadTips()
  }

  fun selectCategory(category: TipCategory?) {
    _uiState.update { it.copy(selectedCategory = if (it.selectedCategory == category) null else category) }
    loadTips()
  }

  fun toggleExpand(tipId: String) {
    _uiState.update {
      it.copy(expandedTipId = if (it.expandedTipId == tipId) null else tipId)
    }
  }

  private fun loadTips() {
    viewModelScope.launch {
      val result = repository.getTipsByCategory(_uiState.value.selectedCategory)
      _uiState.update { it.copy(tips = result) }
    }
  }
}
