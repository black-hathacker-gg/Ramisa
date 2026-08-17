package com.example

import com.example.domain.model.AgeCategory
import com.example.domain.model.UserType
import com.example.features.auth.AuthViewModel
import com.example.features.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthAndProfileUnitTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testAuthViewModel_RegistrationValidation() = runTest(testDispatcher) {
    val authViewModel = AuthViewModel()

    // Test blank name
    var registered = false
    authViewModel.register { registered = true }
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(registered)
    assertNotNull(authViewModel.uiState.value.errorMessage)

    // Fill valid data
    authViewModel.onNameChanged("Nusrat Jahan")
    authViewModel.onPhoneChanged("+880 1812-345678")
    authViewModel.onEmergencyPinChanged("9876")
    authViewModel.onBloodGroupChanged("O+")
    authViewModel.onUserTypeSelected(UserType.ADULT)
    authViewModel.onAgeCategorySelected(AgeCategory.ADULT_18_PLUS)

    authViewModel.register { registered = true }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(registered)
    assertTrue(authViewModel.uiState.value.isAuthenticated)
  }

  @Test
  fun testAuthViewModel_LoginFlow() = runTest(testDispatcher) {
    val authViewModel = AuthViewModel()
    var loggedIn = false

    authViewModel.onPhoneChanged("+880 1712-345678")
    authViewModel.onPasswordChanged("1234")
    authViewModel.login { loggedIn = true }
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(loggedIn)
    assertTrue(authViewModel.uiState.value.isAuthenticated)
  }

  @Test
  fun testProfileViewModel_SaveProfile() = runTest(testDispatcher) {
    val profileViewModel = ProfileViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    profileViewModel.setEditing(true)
    profileViewModel.onEditNameChanged("Tahmina Akter")
    profileViewModel.onEditPhoneChanged("+880 1912-998877")
    profileViewModel.onEditBloodGroupChanged("AB+")
    profileViewModel.onEditEmergencyNoteChanged("Penicillin allergy")
    profileViewModel.onEditGuardianNameChanged("Akter Hossain")
    profileViewModel.onEditGuardianPhoneChanged("+880 1911-001122")

    profileViewModel.saveProfile()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("Tahmina Akter", profileViewModel.uiState.value.user.name)
    assertEquals("AB+", profileViewModel.uiState.value.user.bloodGroup)
    assertEquals("Akter Hossain", profileViewModel.uiState.value.user.guardianName)
    assertFalse(profileViewModel.uiState.value.isEditing)
  }
}
