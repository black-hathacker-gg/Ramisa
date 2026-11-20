package com.example

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.sms.EmergencySmsDispatcher
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RamisaNavigationSmokeTest {
  @get:Rule val compose = createAndroidComposeRule<MainActivity>()

  @Test fun guestCanOpenAllToolsWithoutDispatchingSos() {
    compose.waitUntil(timeoutMillis = 15_000) {
      exists("btn_welcome_guest") || exists("route_home")
    }
    if (exists("btn_welcome_guest")) compose.onNodeWithTag("btn_welcome_guest").performClick()
    waitFor("sos_button")
    capturePreview("home")
    val existingReports = EmergencySmsDispatcher.reports.value.keys
    compose.onNodeWithTag("btn_help_guide").performClick()
    waitFor("ramisa_guide_sheet")
    compose.onNodeWithTag("guide_next").performScrollTo().performClick()
    compose.onNodeWithTag("guide_practice_sos").performScrollTo().performClick()
    waitFor("guide_practice_result")
    assertEquals(existingReports, EmergencySmsDispatcher.reports.value.keys)
    Espresso.pressBack()
    compose.onNodeWithTag("ramisa_guide_sheet").assertDoesNotExist()
    compose.onNodeWithTag("sos_button").performClick()
    waitFor("emergency_title")
    compose.onNodeWithTag("sms_status_card").assertIsDisplayed()
    capturePreview("sos")
    assertEquals(existingReports, EmergencySmsDispatcher.reports.value.keys)
    Espresso.pressBack()
    waitFor("route_home")

    compose.onNodeWithTag("btn_all_tools").performScrollTo().performClick()
    waitFor("route_tools_hub")
    capturePreview("tools")
    val tools = listOf(
      Tool("Emergency contacts", "contacts", "emergency_contacts"),
      Tool("Fake call", "fake_call"),
      Tool("Siren & strobe", "siren_strobe"),
      Tool("Lock screen SOS", "lockscreen_sos", "lock_screen_sos"),
      Tool("Offline mesh", "mesh_network"),
      Tool("Safe journey", "safe_journey"),
      Tool("Safety map", "danger_map"),
      Tool("Safe places", "safe_places"),
      Tool("Ride safety", "ride_safety"),
      Tool("Safety timer", "safety_timer"),
      Tool("Offline maps", "offline_maps"),
      Tool("AI Threat Guard", "threat_guard"),
      Tool("Hardware SOS", "hardware_trigger"),
      Tool("Low-battery auto-ping", "battery_ping"),
      Tool("Stealth calculator", "stealth_mode"),
      Tool("Incident history", "history"),
      Tool("Community safety", "community_safety"),
      Tool("Volunteer escort", "volunteer_escort"),
      Tool("Safety guide & rights", "legal_guide", "safety_guide"),
      Tool("Legal aid & GD", "legal_aid"),
      Tool("Cyber harassment support", "cyber_crime", "cyber_crime_support"),
      Tool("First aid", "first_aid"),
      Tool("Wellness & counseling", "wellness", "wellness_counseling"),
      Tool("Self-defense guides", "self_defense")
    )
    for (tool in tools) {
      waitFor("search_tools")
      compose.onNodeWithTag("search_tools").performTextReplacement(tool.label)
      compose.onNodeWithTag("search_tools").performImeAction()
      val tag = "tool_${tool.tag}"
      waitFor(tag)
      compose.onNodeWithTag(tag).performClick()
      waitFor("route_${tool.route}")
      if (tool.route == "danger_map" && capturePreviews) {
        waitFor("canvas_danger_heatmap")
        compose.waitUntil(timeoutMillis = 25_000) {
          compose.onAllNodesWithText("Loading map", useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("map_tiles_unavailable").assertDoesNotExist()
        capturePreview("map")
      }
      Espresso.pressBack()
      waitFor("route_tools_hub")
    }
  }

  private fun exists(tag: String) = compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

  private fun waitFor(tag: String) {
    compose.waitUntil(timeoutMillis = 8_000) { exists(tag) }
    compose.onNodeWithTag(tag).assertIsDisplayed()
  }

  private val capturePreviews
    get() = InstrumentationRegistry.getArguments().getString("capturePreviews") == "true"

  private fun capturePreview(name: String) {
    if (!capturePreviews) return
    compose.waitForIdle()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    // Let the emulator composite Compose and MapLibre's native texture into the same frame.
    instrumentation.waitForIdleSync()
    SystemClock.sleep(350)
    val screenshot = instrumentation.uiAutomation.takeScreenshot() ?: return
    File(instrumentation.targetContext.cacheDir, "ramisa-preview-$name.png").outputStream().use {
      screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    screenshot.recycle()
  }

  private data class Tool(val label: String, val tag: String, val route: String = tag)
}
