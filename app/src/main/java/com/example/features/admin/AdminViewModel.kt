package com.example.features.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.AdminRepository
import com.example.domain.model.AdminUserRecord
import com.example.domain.model.SafetyBroadcastMessage
import com.example.domain.model.UserVerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AdminFilter {
  ALL,
  PENDING,
  VERIFIED,
  SUSPENDED
}

data class AdminUiState(
  val isAuthenticated: Boolean = false,
  val adminPinInput: String = "",
  val authError: String? = null,
  val selectedUserForDetail: AdminUserRecord? = null,
  val selectedFilter: AdminFilter = AdminFilter.ALL,
  val searchQuery: String = "",
  val showBroadcastDialog: Boolean = false,
  val showExportDialog: Boolean = false,
  val showVerificationDialog: Boolean = false,
  val exportDataContent: String = "",
  val statusMessage: String? = null
)

class AdminViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val repository = AdminRepository(
    context = application,
    adminUserDao = database.adminUserDao(),
    safetyBroadcastDao = database.safetyBroadcastDao()
  )

  private val _uiState = MutableStateFlow(AdminUiState())
  val uiState: StateFlow<AdminUiState> = _uiState

  val users: StateFlow<List<AdminUserRecord>> = repository.users
  val broadcasts: StateFlow<List<SafetyBroadcastMessage>> = repository.broadcasts

  val filteredUsers: StateFlow<List<AdminUserRecord>> = combine(
    users,
    _uiState
  ) { userList, state ->
    userList.filter { user ->
      val matchesFilter = when (state.selectedFilter) {
        AdminFilter.ALL -> true
        AdminFilter.PENDING -> user.verificationStatus == UserVerificationStatus.PENDING
        AdminFilter.VERIFIED -> user.verificationStatus == UserVerificationStatus.VERIFIED
        AdminFilter.SUSPENDED -> user.verificationStatus == UserVerificationStatus.SUSPENDED
      }

      val matchesQuery = if (state.searchQuery.isBlank()) {
        true
      } else {
        val q = state.searchQuery.trim().lowercase()
        user.name.lowercase().contains(q) ||
          user.phone.lowercase().contains(q) ||
          user.idNumber.lowercase().contains(q) ||
          user.institutionOrArea.lowercase().contains(q) ||
          user.emergencyGuardianName.lowercase().contains(q) ||
          user.emergencyGuardianPhone.lowercase().contains(q)
      }

      matchesFilter && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun onPinChange(pin: String) {
    _uiState.value = _uiState.value.copy(adminPinInput = pin, authError = null)
  }

  fun authenticateAdmin(onSuccess: () -> Unit = {}) {
    // Master Admin PIN is 2304
    val pin = _uiState.value.adminPinInput.trim()
    if (repository.verifyMasterPin(pin)) {
      _uiState.value = _uiState.value.copy(
        isAuthenticated = true,
        authError = null,
        adminPinInput = "",
        statusMessage = "🛡️ SuperAdmin Access Granted (Control Room Active)"
      )
      onSuccess()
    } else {
      _uiState.value = _uiState.value.copy(
        authError = "ভুল মাস্টার পিন! অনুগ্রহ করে সঠিক অ্যাডমিন পিন দিন (মাস্টার পিন: 2304)"
      )
    }
  }

  fun logoutAdmin() {
    _uiState.value = AdminUiState(isAuthenticated = false)
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun setFilter(filter: AdminFilter) {
    _uiState.value = _uiState.value.copy(selectedFilter = filter)
  }

  fun selectUser(user: AdminUserRecord?) {
    _uiState.value = _uiState.value.copy(selectedUserForDetail = user)
  }

  fun openVerificationDialog(user: AdminUserRecord) {
    _uiState.value = _uiState.value.copy(
      selectedUserForDetail = user,
      showVerificationDialog = true
    )
  }

  fun closeVerificationDialog() {
    _uiState.value = _uiState.value.copy(showVerificationDialog = false)
  }

  fun approveUserVerification(userId: String, note: String = "Identity & Guardian Phone Verified by Admin") {
    repository.updateUserVerification(userId, UserVerificationStatus.VERIFIED, note)
    closeVerificationDialog()
    _uiState.value = _uiState.value.copy(
      statusMessage = "✅ ইউজার সফলভাবে ভেরিফাইড হিসেবে অনুমোদিত ও সংরক্ষিত হয়েছে!"
    )
  }

  fun rejectUserVerification(userId: String, reason: String = "Incomplete/Invalid Student ID document") {
    repository.updateUserVerification(userId, UserVerificationStatus.REJECTED, reason)
    closeVerificationDialog()
    _uiState.value = _uiState.value.copy(
      statusMessage = "❌ ইউজারের ভেরিফিকেশন বাতিল করা হয়েছে এবং ডাটাবেজে সংরক্ষিত হয়েছে।"
    )
  }

  fun toggleSuspendUser(userId: String, reason: String) {
    repository.toggleUserSuspension(userId, reason)
    _uiState.value = _uiState.value.copy(
      statusMessage = "⚠️ ইউজারের অ্যাক্সেস স্ট্যাটাস আপডেট ও সংরক্ষিত হয়েছে।"
    )
  }

  fun updateAdminNote(userId: String, note: String) {
    repository.updateAdminNote(userId, note)
    _uiState.value = _uiState.value.copy(
      statusMessage = "📝 অ্যাডমিন নোট ডাটাবেজে সেভ করা হয়েছে।"
    )
  }

  fun openBroadcastDialog() {
    _uiState.value = _uiState.value.copy(showBroadcastDialog = true)
  }

  fun closeBroadcastDialog() {
    _uiState.value = _uiState.value.copy(showBroadcastDialog = false)
  }

  fun sendBroadcastAlert(title: String, message: String, priority: String) {
    repository.sendBroadcastAlert(title, message, priority)
    closeBroadcastDialog()
    _uiState.value = _uiState.value.copy(
      statusMessage = "📢 জরুরি নিরাপত্তা বার্তা ব্রডকাস্ট এবং লোকাল ডাটাবেজে স্থায়ীভাবে সংরক্ষিত হয়েছে!"
    )
  }

  fun openExportDialog() {
    val csv = repository.generateRosterCsv()
    _uiState.value = _uiState.value.copy(
      showExportDialog = true,
      exportDataContent = csv
    )
  }

  fun closeExportDialog() {
    _uiState.value = _uiState.value.copy(showExportDialog = false)
  }

  fun clearStatusMessage() {
    _uiState.value = _uiState.value.copy(statusMessage = null)
  }
}
