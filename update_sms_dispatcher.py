with open("app/src/main/java/com/example/core/sms/EmergencySmsDispatcher.kt", "r") as f:
    content = f.read()

# Replace SmsManager resolution to robust multi-step fallback
old_block = """            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val dividedParts = smsManager.divideMessage(messageBody)

            for (contact in contacts) {
                val cleanPhone = contact.phone.replace(Regex("[^0-9+]"), "")
                if (cleanPhone.isNotBlank()) {
                    try {
                        smsManager.sendMultipartTextMessage(
                            cleanPhone,
                            null,
                            dividedParts,
                            null,
                            null
                        )
                        successNames.add(contact.name)
                        Log.d(TAG, "SMS successfully dispatched to ${contact.name} ($cleanPhone)")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send SMS to ${contact.name} ($cleanPhone): ${e.message}")
                        failedNames.add("${contact.name}: ${e.message ?: "Failed"}")
                    }
                }
            }"""

new_block = """            val smsManager: SmsManager = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            for (contact in contacts) {
                val cleanPhone = contact.phone.replace(Regex("[^0-9+]"), "")
                if (cleanPhone.isNotBlank()) {
                    var sentSuccessfully = false
                    try {
                        val dividedParts = smsManager.divideMessage(messageBody)
                        if (dividedParts.size > 1) {
                            smsManager.sendMultipartTextMessage(
                                cleanPhone,
                                null,
                                dividedParts,
                                null,
                                null
                            )
                        } else {
                            smsManager.sendTextMessage(
                                cleanPhone,
                                null,
                                messageBody,
                                null,
                                null
                            )
                        }
                        sentSuccessfully = true
                        successNames.add(contact.name)
                        Log.d(TAG, "SMS successfully dispatched to ${contact.name} ($cleanPhone)")
                    } catch (e: Exception) {
                        Log.w(TAG, "Primary SMS sending failed for ${contact.name}, trying default SmsManager: ${e.message}")
                        try {
                            @Suppress("DEPRECATION")
                            val fallbackSms = SmsManager.getDefault()
                            fallbackSms.sendTextMessage(cleanPhone, null, messageBody.take(160), null, null)
                            sentSuccessfully = true
                            successNames.add(contact.name)
                        } catch (e2: Exception) {
                            Log.e(TAG, "Failed to send SMS to ${contact.name} ($cleanPhone): ${e2.message}")
                            failedNames.add("${contact.name}: ${e2.message ?: "Failed"}")
                        }
                    }
                }
            }"""

content = content.replace(old_block, new_block)

with open("app/src/main/java/com/example/core/sms/EmergencySmsDispatcher.kt", "w") as f:
    f.write(content)

print("Updated EmergencySmsDispatcher successfully")
