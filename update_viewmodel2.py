with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

email_validation = """    if (state.email.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Please enter your email address") }
      return
    }
    if (state.password.isBlank() || state.password.length < 6) {
      _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
      return
    }"""

content = content.replace("if (state.name.isBlank()) {", email_validation + "\n    if (state.name.isBlank()) {")

with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
print("AuthViewModel updated again")
