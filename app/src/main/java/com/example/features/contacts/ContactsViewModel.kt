package com.example.features.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EmergencyContactRepositoryImpl
import com.example.domain.model.EmergencyContact
import com.example.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactsUiState(
  val contacts: List<EmergencyContact> = emptyList(),
  val maxContactsLimit: Int = 7,
  val isAddDialogVisible: Boolean = false,
  val feedbackMessage: String? = null
)

class ContactsViewModel(
  private val repository: EmergencyContactRepository = EmergencyContactRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ContactsUiState())
  val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      repository.contacts.collect { list ->
        _uiState.update { it.copy(contacts = list) }
      }
    }
  }

  fun showAddDialog(show: Boolean) {
    _uiState.update { it.copy(isAddDialogVisible = show) }
  }

  fun addContact(name: String, phone: String, relationship: String) {
    if (_uiState.value.contacts.size >= _uiState.value.maxContactsLimit) {
      _uiState.update { it.copy(feedbackMessage = "Maximum 7 emergency contacts allowed") }
      return
    }
    viewModelScope.launch {
      val res = repository.addContact(name, phone, relationship)
      if (res.isSuccess) {
        _uiState.update {
          it.copy(isAddDialogVisible = false, feedbackMessage = "Contact added successfully")
        }
      } else {
        _uiState.update {
          it.copy(feedbackMessage = res.exceptionOrNull()?.message ?: "Failed to add contact")
        }
      }
    }
  }

  fun deleteContact(contactId: String) {
    viewModelScope.launch {
      repository.deleteContact(contactId)
      _uiState.update { it.copy(feedbackMessage = "Contact removed") }
    }
  }

  fun toggleSms(contactId: String) {
    viewModelScope.launch {
      repository.toggleSms(contactId)
    }
  }

  fun toggleCall(contactId: String) {
    viewModelScope.launch {
      repository.toggleCall(contactId)
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
