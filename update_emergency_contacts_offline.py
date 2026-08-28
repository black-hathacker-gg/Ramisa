with open("app/src/main/java/com/example/features/contacts/EmergencyContactsViewModel.kt", "r") as f:
    content = f.read()

# Make sure Supabase failure never blocks or throws in saveContact, deleteContact, toggleTrusted
# Replace try { Supabase... } with completely non-blocking IO launches
print("Length of EmergencyContactsViewModel:", len(content))
