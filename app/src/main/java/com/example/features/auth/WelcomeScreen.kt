package com.example.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.repository.AppSessionManager

@Composable
fun WelcomeScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToRegister: () -> Unit,
  onContinueAsGuest: () -> Unit = {},
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
  errorMessage: String? = null
) {
  val isBangla by AppSessionManager.isBanglaLanguage.collectAsStateWithLifecycle()

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(contentAlignment = Alignment.Center) {
      Column(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
          .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Image(
            painter = painterResource(R.drawable.img_app_logo),
            contentDescription = null,
            modifier = Modifier.size(72.dp).clip(CircleShape)
          )
          Text("RAMISA", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
          Text(
            text = if (isBangla) "বাংলাদেশের নারী ও শিশুদের ব্যক্তিগত নিরাপত্তা"
              else "Personal safety for women and children in Bangladesh",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Button(
            onClick = onNavigateToRegister,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("btn_welcome_register"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(if (isBangla) "অ্যাকাউন্ট তৈরি করুন" else "Create account")
          }
          OutlinedButton(
            onClick = onNavigateToLogin,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("btn_welcome_login"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(if (isBangla) "সাইন ইন" else "Sign in")
          }
          TextButton(
            onClick = onContinueAsGuest,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().testTag("btn_welcome_guest")
          ) {
            Text(if (isBangla) "অতিথি হিসেবে ব্যবহার করুন" else "Continue as guest")
          }
          if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          errorMessage?.let { error ->
            Text(
              text = error,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
          }
        }
      }
    }
  }
}
