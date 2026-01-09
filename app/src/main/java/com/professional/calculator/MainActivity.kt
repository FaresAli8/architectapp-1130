package com.professional.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.professional.calculator.ui.theme.ProfessionalCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfessionalCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    val viewModel = viewModel<CalculatorViewModel>()
    val state by viewModel.uiState.collectAsState()
    val history = viewModel.history

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Toggle History Button
                IconButton(
                    onClick = { viewModel.onAction(CalculatorAction.ToggleHistory) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                   Text(text = "🕒", fontSize = 20.sp)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = state.expression,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.result,
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }

            // Buttons Area
            Column(modifier = Modifier.weight(1.5f)) {
                val buttonSpacing = 10.dp
                
                // Advanced Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton(symbol = "AC", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.errorContainer) { viewModel.onAction(CalculatorAction.Clear) }
                    CalculatorButton(symbol = "√", modifier = Modifier.weight(1f)) { viewModel.onAction(CalculatorAction.Symbol("√")) }
                    CalculatorButton(symbol = "^", modifier = Modifier.weight(1f)) { viewModel.onAction(CalculatorAction.Symbol("^")) }
                    CalculatorButton(symbol = "%", modifier = Modifier.weight(1f)) { viewModel.onAction(CalculatorAction.Symbol("%")) }
                }
                Spacer(modifier = Modifier.height(buttonSpacing))

                // Standard Rows
                val rows = listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "-"),
                    listOf(".", "0", "DEL", "+")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        for (symbol in row) {
                            val isOperator = symbol in listOf("÷", "×", "-", "+")
                            val isAction = symbol == "DEL"
                            
                            CalculatorButton(
                                symbol = symbol,
                                modifier = Modifier.weight(1f),
                                color = if (isOperator) MaterialTheme.colorScheme.primaryContainer 
                                        else if (isAction) MaterialTheme.colorScheme.tertiaryContainer 
                                        else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                when (symbol) {
                                    "DEL" -> viewModel.onAction(CalculatorAction.Delete)
                                    "÷" -> viewModel.onAction(CalculatorAction.Symbol("÷"))
                                    "×" -> viewModel.onAction(CalculatorAction.Symbol("×"))
                                    else -> {
                                        if (symbol.toIntOrNull() != null) {
                                            viewModel.onAction(CalculatorAction.Number(symbol.toInt()))
                                        } else {
                                            viewModel.onAction(CalculatorAction.Symbol(symbol))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(buttonSpacing))
                }

                // Equals Button
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.onAction(CalculatorAction.Calculate) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = "=", fontSize = 32.sp)
                    }
                }
            }
        }

        // History Overlay
        AnimatedVisibility(
            visible = state.historyVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.history),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { viewModel.onAction(CalculatorAction.ToggleHistory) }) {
                            Text(text = "✕", fontSize = 24.sp)
                        }
                    }
                    Divider()
                    
                    if (history.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(R.string.no_history))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(history) { item ->
                                HistoryItemRow(item) {
                                    viewModel.onAction(CalculatorAction.LoadHistoryItem(item))
                                }
                            }
                        }
                        Button(
                            onClick = { viewModel.onAction(CalculatorAction.ClearHistory) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.clear_history))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = contentColorFor(color)
        )
    }
}

@Composable
fun HistoryItemRow(item: HistoryItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = item.expression,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "= ${item.result}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}