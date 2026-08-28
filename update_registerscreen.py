with open("app/src/main/java/com/example/features/auth/RegisterScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.Person", "import androidx.compose.material.icons.filled.Email\nimport androidx.compose.material.icons.filled.Person")

email_input = """      Spacer(modifier = Modifier.height(12.dp))

      // Email Input
      OutlinedTextField(
        value = uiState.email,
        onValueChange = viewModel::onEmailChanged,
        label = { Text("Email Address") },
        placeholder = { Text("your.email@example.com") },
        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_reg_email")
      )"""

content = content.replace("// Phone Number", email_input + "\n\n      // Phone Number")

with open("app/src/main/java/com/example/features/auth/RegisterScreen.kt", "w") as f:
    f.write(content)
print("RegisterScreen updated")
