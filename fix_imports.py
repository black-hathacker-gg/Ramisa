import re

with open('app/src/main/java/com/example/features/fakecall/FakeCallScreen.kt', 'r') as f:
    content = f.read()

# Remove any weird merged imports at the start
content = re.sub(r'^(?:import[^\n]+?)+package', 'package', content)

# Add the correct imports after package
imports = """
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
"""
content = content.replace('package com.example.features.fakecall', 'package com.example.features.fakecall\n' + imports)

with open('app/src/main/java/com/example/features/fakecall/FakeCallScreen.kt', 'w') as f:
    f.write(content)
