package com.example.domain.repository

import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import kotlinx.coroutines.flow.StateFlow

sealed interface AuthResult<out T> {
  data class Success<T>(val data: T) : AuthResult<T>
  data class Error(val message: String) : AuthResult<Nothing>
}

interface AuthRepository {
  val currentUser: StateFlow<UserProfile?>
  val isAuthenticated: StateFlow<Boolean>

  suspend fun login(phone: String, pinOrPassword: String): AuthResult<UserProfile>
  suspend fun register(
    name: String,
    phone: String,
    password: String,
    emergencyPin: String,
    userType: UserType,
    ageCategory: AgeCategory,
    bloodGroup: String = "Unknown",
    emergencyNote: String = ""
  ): AuthResult<UserProfile>
  suspend fun logout(): Boolean
  suspend fun restoreSession(): Boolean
}
