with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "r") as f:
    lines = f.readlines()

lines[70] = "    if (state.email.isBlank()) {\n"
lines[110] = "    if (state.email.isBlank()) {\n"
lines[122] = "    if (state.phone.isBlank()) {\n"

with open("app/src/main/java/com/example/features/auth/AuthViewModel.kt", "w") as f:
    f.writelines(lines)
