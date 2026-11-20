package com.example.features.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.EmergencyContactRepositoryImpl
import com.example.domain.model.EmergencyContact
import com.example.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactsUiState(
  val contacts: List<EmergencyContact> = emptyList(),
  val maxContactsLimit: Int = 7,
  val isAddDialogVisible: Boolean = false,
  val isSavingContact: Boolean = false,
  val feedbackMessage: String? = null
)

class ContactsViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository: EmergencyContactRepository = EmergencyContactRepositoryImpl(
      AppDatabase.getDatabase(application).emergencyContactDao()
  )

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
    _uiState.update { it.copy(isAddDialogVisible = show, feedbackMessage = null) }
  }

  fun addContact(name: String, phone: String, relationship: String) {
    if (_uiState.value.isSavingContact) return
    if (_uiState.value.contacts.size >= _uiState.value.maxContactsLimit) {
      _uiState.update { it.copy(feedbackMessage = "Maximum 7 emergency contacts allowed") }
      return
    }
    _uiState.update { it.copy(isSavingContact = true, feedbackMessage = null) }
    viewModelScope.launch {
      try {
        repository.addContact(name, phone, relationship).getOrThrow()
        _uiState.update {
          it.copy(isAddDialogVisible = false, feedbackMessage = "Contact added successfully")
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        _uiState.update {
          it.copy(feedbackMessage = error.message ?: "Could not add the contact.")
        }
      } finally {
        _uiState.update { it.copy(isSavingContact = false) }
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
