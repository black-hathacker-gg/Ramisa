package com.example.features.stealth

import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.MathContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StealthModeUiState(
  val displayText: String = "0",
  val formulaText: String = ""
)

class StealthModeViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(StealthModeUiState())
  val uiState: StateFlow<StealthModeUiState> = _uiState.asStateFlow()

  private var currentNumber = ""
  private var firstOperand: BigDecimal? = null
  private var pendingOperator: String? = null
  private var rawInput = true

  fun onDigit(digit: String) {
    if (pendingOperator == null && !rawInput) currentNumber = ""
    if (currentNumber.length >= 16) return
    currentNumber += digit
    rawInput = true
    updateDisplay()
  }

  fun onOperator(operator: String) {
    val operand = currentNumber.toBigDecimalOrNull()
    if (operand != null) {
      val previous = firstOperand
      val previousOperator = pendingOperator
      val combined = if (previous != null && previousOperator != null) {
        calculate(previous, operand, previousOperator)
      } else operand
      if (combined == null) {
        showError()
        return
      }
      firstOperand = combined
      currentNumber = ""
    }
    if (firstOperand == null) return
    pendingOperator = operator
    rawInput = false
    updateDisplay()
  }

  fun onDecimal() {
    if (pendingOperator == null && !rawInput) currentNumber = ""
    if (currentNumber.contains('.') || currentNumber.length >= 16) return
    currentNumber = currentNumber.ifEmpty { "0" } + '.'
    rawInput = true
    updateDisplay()
  }

  fun changeSign() {
    val input = currentNumber.toBigDecimalOrNull() ?: return
    currentNumber = input.negate().asDisplay()
    rawInput = false
    updateDisplay()
  }

  fun percent() {
    val input = currentNumber.toBigDecimalOrNull() ?: return
    currentNumber = input.movePointLeft(2).asDisplay()
    rawInput = false
    updateDisplay()
  }

  fun onClear() {
    currentNumber = ""
    firstOperand = null
    pendingOperator = null
    rawInput = true
    _uiState.value = StealthModeUiState()
  }

  fun onEquals(
    unlockPin: String?,
    onSecretSos: () -> Unit,
    onUnlockApp: () -> Unit
  ) {
    if (pendingOperator == null && rawInput) {
      if (!unlockPin.isNullOrBlank() && currentNumber == unlockPin) {
        onClear()
        onUnlockApp()
        return
      }
      if (currentNumber in setOf("9999", "911", "999")) {
        onClear()
        onSecretSos()
        return
      }
    }

    val first = firstOperand ?: return
    val second = currentNumber.toBigDecimalOrNull() ?: return
    val operator = pendingOperator ?: return
    val result = calculate(first, second, operator)
    if (result == null) {
      showError()
      return
    }

    currentNumber = result.asDisplay()
    firstOperand = null
    pendingOperator = null
    rawInput = false
    updateDisplay()
  }

  private fun updateDisplay() {
    val first = firstOperand?.asDisplay()
    _uiState.value = StealthModeUiState(
      displayText = currentNumber.ifEmpty { first ?: "0" },
      formulaText = if (pendingOperator != null) "$first $pendingOperator" else ""
    )
  }

  private fun showError() {
    onClear()
    _uiState.value = StealthModeUiState(displayText = "Error")
  }

  private fun calculate(first: BigDecimal, second: BigDecimal, operator: String): BigDecimal? = when (operator) {
    "+" -> first.add(second, MathContext.DECIMAL64)
    "-" -> first.subtract(second, MathContext.DECIMAL64)
    "×" -> first.multiply(second, MathContext.DECIMAL64)
    "÷" -> if (second.signum() == 0) null else first.divide(second, MathContext.DECIMAL64)
    else -> null
  }

  private fun BigDecimal.asDisplay(): String {
    val value = stripTrailingZeros()
    return if (value.precision() - value.scale() in -8..18) {
      value.toPlainString()
    } else {
      value.toEngineeringString()
    }
  }
}
