package com.example

import com.example.features.stealth.StealthModeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StealthCalculatorUnitTest {
  @Test fun `leading zero PIN can unlock and old default cannot`() {
    val calculator = StealthModeViewModel()
    var unlocked = false
    calculator.enter("1234")
    calculator.onEquals("0042", onSecretSos = {}, onUnlockApp = { unlocked = true })
    assertFalse(unlocked)
    calculator.onClear()
    calculator.enter("0042")
    calculator.onEquals("0042", onSecretSos = {}, onUnlockApp = { unlocked = true })
    assertTrue(unlocked)
    assertEquals("0", calculator.uiState.value.displayText)
  }

  @Test fun `shortcuts only run for a directly entered value`() {
    val calculator = StealthModeViewModel()
    var alerts = 0
    var unlocked = false
    calculator.enter("1")
    calculator.onOperator("+")
    calculator.enter("999")
    calculator.onEquals("1000", onSecretSos = { alerts++ }, onUnlockApp = { unlocked = true })
    calculator.onEquals("1000", onSecretSos = { alerts++ }, onUnlockApp = { unlocked = true })
    assertFalse(unlocked)
    assertEquals(0, alerts)
    assertEquals("1000", calculator.uiState.value.displayText)

    calculator.enter("999")
    calculator.onEquals("1000", onSecretSos = { alerts++ }, onUnlockApp = { unlocked = true })
    assertEquals(1, alerts)
  }

  @Test fun `sign and percent keys work and division by zero shows an error`() {
    val calculator = StealthModeViewModel()
    calculator.enter("25")
    calculator.changeSign()
    calculator.percent()
    assertEquals("-0.25", calculator.uiState.value.displayText)
    calculator.onOperator("÷")
    calculator.enter("0")
    calculator.onEquals(null, onSecretSos = {}, onUnlockApp = {})
    assertEquals("Error", calculator.uiState.value.displayText)
  }

  private fun StealthModeViewModel.enter(digits: String) = digits.forEach { onDigit(it.toString()) }
}
