package com.professional.calculator

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DecimalFormat

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val historyVisible: Boolean = false
)

data class HistoryItem(val expression: String, val result: String)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _history = mutableStateListOf<HistoryItem>()
    val history: List<HistoryItem> = _history

    private val decimalFormat = DecimalFormat("#.########")

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> append(action.number.toString())
            is CalculatorAction.Symbol -> append(action.symbol)
            is CalculatorAction.Clear -> {
                _uiState.update { it.copy(expression = "", result = "") }
            }
            is CalculatorAction.Delete -> deleteLast()
            is CalculatorAction.Calculate -> calculate()
            is CalculatorAction.ToggleHistory -> {
                _uiState.update { it.copy(historyVisible = !it.historyVisible) }
            }
            is CalculatorAction.ClearHistory -> {
                _history.clear()
            }
            is CalculatorAction.LoadHistoryItem -> {
                _uiState.update { it.copy(expression = action.item.expression, result = "", historyVisible = false) }
            }
        }
    }

    private fun append(char: String) {
        _uiState.update {
            it.copy(expression = it.expression + char)
        }
    }

    private fun deleteLast() {
        val currentExp = _uiState.value.expression
        if (currentExp.isNotEmpty()) {
            _uiState.update {
                it.copy(expression = currentExp.dropLast(1))
            }
        }
    }

    private fun calculate() {
        val expression = _uiState.value.expression
        if (expression.isBlank()) return

        try {
            // Replace visual symbols with engine compatible ones
            val engineExpression = expression
                .replace("×", "*")
                .replace("÷", "/")
            
            val resultValue = CalculatorEngine.evaluate(engineExpression)
            val formattedResult = decimalFormat.format(resultValue)

            // Add to history
            _history.add(0, HistoryItem(expression, formattedResult))

            _uiState.update {
                it.copy(result = formattedResult)
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(result = "Error")
            }
        }
    }
}

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Symbol(val symbol: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
    object ToggleHistory : CalculatorAction()
    object ClearHistory : CalculatorAction()
    data class LoadHistoryItem(val item: HistoryItem) : CalculatorAction()
}