package com.example.features.stealth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StealthModeUiState(
  val displayText: String = "0",
  val formulaText: String = "",
  val isSecretSosTriggered: Boolean = false,
  val feedbackToast: String? = null
)

class StealthModeViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(StealthModeUiState())
  val uiState: StateFlow<StealthModeUiState> = _uiState.asStateFlow()

  private var currentNumber = ""
  private var firstOperand = 0.0
  private var pendingOp = ""

  fun onDigit(digit: String) {
    if (currentNumber == "0") currentNumber = ""
    currentNumber += digit
    _uiState.update {
      it.copy(
        displayText = currentNumber,
        formulaText = if (pendingOp.isNotEmpty()) "$firstOperand $pendingOp $currentNumber" else currentNumber
      )
    }
  }

  fun onOperator(op: String) {
    if (currentNumber.isNotEmpty()) {
      firstOperand = currentNumber.toDoubleOrNull() ?: 0.0
      pendingOp = op
      currentNumber = ""
      _uiState.update {
        it.copy(formulaText = "$firstOperand $pendingOp")
      }
    }
  }

  fun onDecimal() {
    if (!currentNumber.contains(".")) {
      if (currentNumber.isEmpty()) currentNumber = "0"
      currentNumber += "."
      _uiState.update { it.copy(displayText = currentNumber) }
    }
  }

  fun onClear() {
    currentNumber = ""
    firstOperand = 0.0
    pendingOp = ""
    _uiState.update {
      it.copy(
        displayText = "0",
        formulaText = "",
        feedbackToast = null
      )
    }
  }

  fun onEquals(
    onSecretSos: () -> Unit,
    onUnlockApp: () -> Unit
  ) {
    // Check for Secret SOS Duress Codes
    if (currentNumber == "9999" || currentNumber == "911" || currentNumber == "999") {
      _uiState.update {
        it.copy(
          isSecretSosTriggered = true,
          feedbackToast = "✓ Calc sync complete" // Discreet decoy message
        )
      }
      onSecretSos()
      return
    }

    // Check for Unlock Code to exit Camouflage
    if (currentNumber == "1234" || currentNumber == "0000") {
      onUnlockApp()
      return
    }

    // Standard Math Execution
    if (pendingOp.isNotEmpty() && currentNumber.isNotEmpty()) {
      val secondOperand = currentNumber.toDoubleOrNull() ?: 0.0
      val result = when (pendingOp) {
        "+" -> firstOperand + secondOperand
        "-" -> firstOperand - secondOperand
        "×", "*" -> firstOperand * secondOperand
        "÷", "/" -> if (secondOperand != 0.0) firstOperand / secondOperand else 0.0
        else -> secondOperand
      }
      val resultStr = if (result % 1.0 == 0.0) result.toInt().toString() else String.format("%.2f", result)
      currentNumber = resultStr
      pendingOp = ""
      _uiState.update {
        it.copy(
          displayText = resultStr,
          formulaText = ""
        )
      }
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackToast = null) }
  }
}
