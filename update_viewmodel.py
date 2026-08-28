with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val phone: String = \"+880 1712-345678\",", "val email: String = \"\",\n  val phone: String = \"\",")
content = content.replace("fun onPhoneChanged(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }", "fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }\n  fun onPhoneChanged(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }")

content = content.replace("if (state.phone.isBlank()) {", "if (state.email.isBlank()) {")
content = content.replace("_uiState.update { it.copy(errorMessage = \"Please enter your phone number\") }", "_uiState.update { it.copy(errorMessage = \"Please enter your email\") }")
content = content.replace("val result = authRepository.login(state.phone, state.password)", "val result = authRepository.login(state.email, state.password)")

content = content.replace("phone = state.phone,", "email = state.email,\n        phone = state.phone,")

with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
print("AuthViewModel updated")
