with open("app/src/main/java/com/example/features/auth/LoginScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.Phone", "import androidx.compose.material.icons.filled.Email\nimport androidx.compose.material.icons.filled.Phone")
content = content.replace("Enter your phone number to access protection", "Enter your email address to access protection")

content = content.replace("uiState.phone", "uiState.email")
content = content.replace("viewModel::onPhoneChanged", "viewModel::onEmailChanged")
content = content.replace("label = { Text(\"Phone Number (বাংলাদেশ)\") }", "label = { Text(\"Email Address\") }")
content = content.replace("placeholder = { Text(\"+880 17XX-XXXXXX\") }", "placeholder = { Text(\"your.email@example.com\") }")
content = content.replace("Icons.Default.Phone", "Icons.Default.Email")
content = content.replace("KeyboardType.Phone", "KeyboardType.Email")
content = content.replace("testTag(\"input_phone\")", "testTag(\"input_email\")")
content = content.replace("// Phone Number Input", "// Email Address Input")

with open("app/src/main/java/com/example/features/auth/LoginScreen.kt", "w") as f:
    f.write(content)
print("LoginScreen updated")
