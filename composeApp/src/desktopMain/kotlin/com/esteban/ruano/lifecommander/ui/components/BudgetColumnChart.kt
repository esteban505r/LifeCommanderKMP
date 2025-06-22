package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat

@Composable
fun BudgetColumnChart(
    budgets: List<BudgetProgress>,
    modifier: Modifier = Modifier,
    onBudgetClick: ((BudgetProgress) -> Unit)? = null
) {
    val cartesianChartModelProducer = remember { CartesianChartModelProducer() }
    val textColor = MaterialTheme.colors.onSurface
    var hoveredBudgetIndex by remember { mutableStateOf<Int?>(null) }
    var mousePosition by remember { mutableStateOf(androidx.compose.ui.unit.IntOffset(0, 0)) }

    LaunchedEffect(budgets) {
        cartesianChartModelProducer.runTransaction {
            if (budgets.isNotEmpty()) {
                // Create data for the column chart
                val budgetNames = budgets.map { it.budget.name }
                val progressValues = budgets.map { 
                    it.progressPercentage.toFloat() // Already returns 0-100
                }

                columnSeries {
                    series(
                        (0 until budgetNames.size).map { it.toFloat() },
                        progressValues
                    )
                }
            }
        }
    }

    Box(modifier = modifier) {
        Column {
            // Chart container with overlay for column interactions
            Box(
                modifier = Modifier.fillMaxWidth().height(280.dp)
            ) {
                // The actual chart
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(
                            valueFormatter = CartesianValueFormatter { _, value, _ ->
                                "${value.toInt()}%"
                            },
                            label = rememberAxisLabelComponent(
                                style = TextStyle(
                                    color = textColor,
                                )
                            ),
                            guideline = rememberAxisGuidelineComponent()
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = CartesianValueFormatter { _, x, _ ->
                                val index = x.toInt()
                                if (index >= 0 && index < budgets.size) {
                                    budgets[index].budget.name.take(10) // Limit name length
                                } else ""
                            },
                            label = rememberAxisLabelComponent(
                                style = TextStyle(
                                    color = textColor,
                                )
                            ),
                            guideline = rememberAxisGuidelineComponent()
                        )
                    ),
                    modelProducer = cartesianChartModelProducer,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Mouse position tracking for the entire chart area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Move) {
                                        val position = event.changes.first().position
                                        mousePosition = androidx.compose.ui.unit.IntOffset(
                                            position.x.toInt(),
                                            position.y.toInt()
                                        )
                                    }
                                }
                            }
                        }
                ) {
                    // Overlay clickable areas for each column - using absolute positioning
                    if (budgets.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxSize().padding(start=42.dp)) {
                            budgets.forEachIndexed { index, budget ->
                                val interactionSource = remember { MutableInteractionSource() }
                                val isHovered by interactionSource.collectIsHoveredAsState()
                                
                                LaunchedEffect(isHovered) {
                                    if (isHovered) {
                                        hoveredBudgetIndex = index
                                    } else if (hoveredBudgetIndex == index) {
                                        hoveredBudgetIndex = null
                                    }
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .hoverable(interactionSource)
                                        .then(
                                            if (onBudgetClick != null) {
                                                Modifier.clickable { onBudgetClick(budget) }
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    // Invisible overlay for interaction
                                }
                            }
                        }
                    }
                }
            }

        }
        
        // Tooltip popup for chart columns at mouse position
        hoveredBudgetIndex?.let { index ->
            val budget = budgets[index]
            Popup(
                alignment = Alignment.TopStart,
                offset = androidx.compose.ui.unit.IntOffset(
                    mousePosition.x + 15, // Offset to the right of cursor
                    mousePosition.y - 15  // Offset above cursor
                ),
                properties = PopupProperties(focusable = false)
            ) {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colors.surface,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = budget.budget.name,
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Progress: ${budget.progressPercentage.toInt()}%",
                            style = MaterialTheme.typography.body2,
                            color = textColor
                        )
                        Text(
                            text = "Spent: ${budget.spent.toCurrencyFormat()}",
                            style = MaterialTheme.typography.body2,
                            color = textColor
                        )
                        Text(
                            text = "Budget: ${budget.budget.amount.toCurrencyFormat()}",
                            style = MaterialTheme.typography.body2,
                            color = textColor
                        )
                        if (budget.isOverBudget) {
                            Text(
                                text = "⚠️ Over Budget!",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.error
                            )
                        }
                    }
                }
            }
        }
    }
} 