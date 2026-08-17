package com.example.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ProfileRepositoryImpl
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
  val user: UserProfile = UserProfile(),
  val isEditing: Boolean = false,
  val editName: String = "",
  val editPhone: String = "",
  val editAgeCategory: AgeCategory = AgeCategory.ADULT_18_PLUS,
  val editUserType: UserType = UserType.ADULT,
  val editBloodGroup: String = "B+",
  val editEmergencyNote: String = "",
  val editGuardianName: String = "",
  val editGuardianPhone: String = "",
  val isLoading: Boolean = false,
  val message: String? = null,
  val errorMessage: String? = null
)

class ProfileViewModel(
  private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUiState())
  val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      profileRepository.profile.collect { profile ->
        _uiState.update { current ->
          current.copy(
            user = profile,
            editName = profile.name,
            editPhone = profile.phone,
            editAgeCategory = profile.ageCategory,
            editUserType = profile.userType,
            editBloodGroup = profile.bloodGroup,
            editEmergencyNote = profile.emergencyNote,
            editGuardianName = profile.guardianName ?: "",
            editGuardianPhone = profile.guardianPhone ?: ""
          )
        }
      }
    }
  }

  fun setEditing(editing: Boolean) {
    val user = _uiState.value.user
    _uiState.update {
      it.copy(
        isEditing = editing,
        editName = user.name,
        editPhone = user.phone,
        editAgeCategory = user.ageCategory,
        editUserType = user.userType,
        editBloodGroup = user.bloodGroup,
        editEmergencyNote = user.emergencyNote,
        editGuardianName = user.guardianName ?: "",
        editGuardianPhone = user.guardianPhone ?: "",
        errorMessage = null,
        message = null
      )
    }
  }

  fun onEditNameChanged(value: String) = _uiState.update { it.copy(editName = value) }
  fun onEditPhoneChanged(value: String) = _uiState.update { it.copy(editPhone = value) }
  fun onEditAgeCategoryChanged(category: AgeCategory) = _uiState.update { it.copy(editAgeCategory = category) }
  fun onEditUserTypeChanged(type: UserType) = _uiState.update { it.copy(editUserType = type) }
  fun onEditBloodGroupChanged(value: String) = _uiState.update { it.copy(editBloodGroup = value) }
  fun onEditEmergencyNoteChanged(value: String) = _uiState.update { it.copy(editEmergencyNote = value) }
  fun onEditGuardianNameChanged(value: String) = _uiState.update { it.copy(editGuardianName = value) }
  fun onEditGuardianPhoneChanged(value: String) = _uiState.update { it.copy(editGuardianPhone = value) }

  fun saveProfile() {
    val state = _uiState.value
    if (state.editName.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
      return
    }
    if (state.editPhone.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Phone number cannot be empty") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null) }
      val result = profileRepository.updateProfile(
        name = state.editName,
        phone = state.editPhone,
        ageCategory = state.editAgeCategory,
        userType = state.editUserType,
        bloodGroup = state.editBloodGroup,
        emergencyNote = state.editEmergencyNote,
        guardianName = state.editGuardianName.ifBlank { null },
        guardianPhone = state.editGuardianPhone.ifBlank { null }
      )
      if (result.isSuccess) {
        _uiState.update {
          it.copy(
            isEditing = false,
            isLoading = false,
            message = "Profile updated successfully"
          )
        }
      } else {
        _uiState.update {
          it.copy(
            isLoading = false,
            errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile"
          )
        }
      }
    }
  }
}
