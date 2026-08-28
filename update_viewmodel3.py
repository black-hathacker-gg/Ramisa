with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("if (state.password.isBlank()) {", "if (state.password.isBlank() || state.password.length < 6) {")
content = content.replace("Please enter your password or PIN", "Password must be at least 6 characters")

with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
print("AuthViewModel updated again again")
