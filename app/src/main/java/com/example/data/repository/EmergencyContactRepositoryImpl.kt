package com.example.data.repository

import com.example.data.local.EmergencyContactDao
import com.example.domain.model.EmergencyContact
import com.example.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class EmergencyContactRepositoryImpl(
    private val dao: EmergencyContactDao
) : EmergencyContactRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val contacts: StateFlow<List<EmergencyContact>> = dao.getAllContacts()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override suspend fun addContact(name: String, phone: String, relationship: String): Result<EmergencyContact> {
        val currentContacts = contacts.value
        if (currentContacts.size >= 7) {
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
            priority = currentContacts.size + 1,
            smsEnabled = true,
            callEnabled = true,
            pushEnabled = true,
            isVerified = true
        )
        dao.insertContact(newContact)
        return Result.success(newContact)
    }

    override suspend fun updateContact(contact: EmergencyContact): Result<EmergencyContact> {
        dao.updateContact(contact)
        return Result.success(contact)
    }

    override suspend fun deleteContact(contactId: String): Result<Boolean> {
        dao.deleteContactById(contactId)
        
        // Reorder priorities after deletion
        val remaining = dao.getAllContacts().first().filterNot { it.id == contactId }
        val updated = remaining.mapIndexed { index, item -> item.copy(priority = index + 1) }
        dao.updateAll(updated)
        
        return Result.success(true)
    }

    override suspend fun reorderContacts(orderedIds: List<String>): Result<Boolean> {
        val currentList = dao.getAllContacts().first()
        val currentMap = currentList.associateBy { it.id }
        val reordered = orderedIds.mapNotNull { currentMap[it] }
            .mapIndexed { index, item -> item.copy(priority = index + 1) }
        dao.updateAll(reordered)
        return Result.success(true)
    }

    override suspend fun toggleSms(contactId: String): Result<Boolean> {
        val contact = dao.getAllContacts().first().find { it.id == contactId }
        if (contact != null) {
            val newState = !contact.smsEnabled
            dao.updateContact(contact.copy(smsEnabled = newState))
            return Result.success(newState)
        }
        return Result.failure(Exception("Contact not found"))
    }

    override suspend fun toggleCall(contactId: String): Result<Boolean> {
        val contact = dao.getAllContacts().first().find { it.id == contactId }
        if (contact != null) {
            val newState = !contact.callEnabled
            dao.updateContact(contact.copy(callEnabled = newState))
            return Result.success(newState)
        }
        return Result.failure(Exception("Contact not found"))
    }
}
