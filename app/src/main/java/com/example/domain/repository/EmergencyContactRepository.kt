package com.example.domain.repository

import com.example.domain.model.EmergencyContact
import kotlinx.coroutines.flow.StateFlow

interface EmergencyContactRepository {
  val contacts: StateFlow<List<EmergencyContact>>

  suspend fun addContact(
    name: String,
    phone: String,
    relationship: String
  ): Result<EmergencyContact>

  suspend fun updateContact(contact: EmergencyContact): Result<EmergencyContact>
  suspend fun deleteContact(contactId: String): Result<Boolean>
  suspend fun reorderContacts(orderedIds: List<String>): Result<Boolean>
  suspend fun toggleSms(contactId: String): Result<Boolean>
  suspend fun toggleCall(contactId: String): Result<Boolean>
}
