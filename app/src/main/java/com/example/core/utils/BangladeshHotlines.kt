package com.example.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object BangladeshHotlines {
  // Bangladesh National Portal emergency directory and the 2025 Legal Aid Department notice.
  const val EMERGENCY = "999"
  const val VIOLENCE_SUPPORT = "109"
  const val CHILD_HELP = "1098"
  const val LEGAL_AID = "16699"
  const val LEGAL_AID_BANGLA = "১৬৬৯৯"

  fun openDialer(context: Context, number: String): Boolean {
    return try {
      context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
      true
    } catch (_: ActivityNotFoundException) {
      false
    }
  }
}
