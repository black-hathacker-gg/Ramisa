package com.example.data.repository

import com.example.domain.model.EmergencyContact
import com.example.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EmergencyContactRepositoryImpl : EmergencyContactRepository {

  private val _contacts = MutableStateFlow<List<EmergencyContact>>(
    listOf(
      EmergencyContact("c1", "Mother (আম্মা)", "+880 1819-112233", "Mother", 1, smsEnabled = true, callEnabled = true, pushEnabled = true, isVerified = true),
      EmergencyContact("c2", "Father (আব্বু)", "+880 1711-223344", "Father", 2, smsEnabled = true, callEnabled = true, pushEnabled = true, isVerified = true),
      EmergencyContact("c3", "Sister (আপু)", "+880 1912-334455", "Sister", 3, smsEnabled = true, callEnabled = true, pushEnabled = false, isVerified = true)
    )
  )
  override val contacts: StateFlow<List<EmergencyContact>> = _contacts.asStateFlow()

  override suspend fun addContact(name: String, phone: String, relationship: String): Result<EmergencyContact> {
    if (_contacts.value.size >= 7) {
      return Result.failure(IllegalStateException("Maximum limit of 7 contacts reached"))
    }
    if (name.isBlank() || phone.isBlank()) {
      return Result.failure(IllegalArgumentException("Name and phone number cannot be blank"))
    }

    val newContact = EmergencyContact(
      id = "c_${System.currentTimeMillis() % 100000}",
      name = name.trim(),
      phone = phone.trim(),
      relationship = relationship.trim(),
      priority = _contacts.value.size + 1,
      smsEnabled = true,
      callEnabled = true,
      pushEnabled = true,
      isVerified = true
    )

    _contacts.update { it + newContact }
    return Result.success(newContact)
  }

  override suspend fun updateContact(contact: EmergencyContact): Result<EmergencyContact> {
    _contacts.update { list ->
      list.map { if (it.id == contact.id) contact else it }
    }
    return Result.success(contact)
  }

  override suspend fun deleteContact(contactId: String): Result<Boolean> {
    _contacts.update { list ->
      list.filterNot { it.id == contactId }
        .mapIndexed { index, item -> item.copy(priority = index + 1) }
    }
    return Result.success(true)
  }

  override suspend fun reorderContacts(orderedIds: List<String>): Result<Boolean> {
    val currentMap = _contacts.value.associateBy { it.id }
    val reordered = orderedIds.mapNotNull { currentMap[it] }
      .mapIndexed { index, item -> item.copy(priority = index + 1) }
    _contacts.value = reordered
    return Result.success(true)
  }

  override suspend fun toggleSms(contactId: String): Result<Boolean> {
    var newState = false
    _contacts.update { list ->
      list.map {
        if (it.id == contactId) {
          newState = !it.smsEnabled
          it.copy(smsEnabled = newState)
        } else it
      }
    }
    return Result.success(newState)
  }

  override suspend fun toggleCall(contactId: String): Result<Boolean> {
    var newState = false
    _contacts.update { list ->
      list.map {
        if (it.id == contactId) {
          newState = !it.callEnabled
          it.copy(callEnabled = newState)
        } else it
      }
    }
    return Result.success(newState)
  }
}
