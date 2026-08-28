import re

with open("app/src/main/java/com/example/domain/repository/AuthRepository.kt", "r") as f:
    content = f.read()

content = content.replace("suspend fun login(phone: String, pinOrPassword: String)", "suspend fun login(email: String, pinOrPassword: String)")
content = content.replace("suspend fun register(\n    name: String,\n    phone: String,", "suspend fun register(\n    name: String,\n    email: String,\n    phone: String,")

with open("app/src/main/java/com/example/domain/repository/AuthRepository.kt", "w") as f:
    f.write(content)
print("AuthRepository updated")
