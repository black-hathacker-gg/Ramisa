package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.features.home.HomeScreen
import com.example.features.home.HomeViewModel
import com.example.ui.theme.RamisaTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun home_screenshot() {
    composeTestRule.setContent {
      RamisaTheme {
        HomeScreen(
          viewModel = HomeViewModel(),
          onNavigateToSafeJourney = {},
          onNavigateToContacts = {},
          onNavigateToHistory = {},
          onNavigateToSettings = {},
          onTriggerSos = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

