package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlinx.datetime.*

data class ChartSeries(
    val name: String,
    val data: List<Int>,
    val color: Color
)

private val xToDateMapKey = ExtraStore.Key<Map<Float, LocalDate>>()

@Composable
fun StatsChart(
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    val cartesianChartModelProducer = remember { CartesianChartModelProducer() }

    // Calculate the start of the current week
    val weekStart = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            .minus(DatePeriod(days = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.value - 1))
    }

    LaunchedEffect(series) {
        cartesianChartModelProducer.runTransaction {
            // Create the date mapping for x-axis formatting
            val xToDates = (0..6).associate { index ->
                index.toFloat() to weekStart.plus(DatePeriod(days = index))
            }
            
            // Add each series
            series.forEach { chartSeries ->
                // Ensure we have 7 data points (one for each day of the week)
                val data = if (chartSeries.data.isEmpty()) {
                    List(7) { 0 } // Fallback to zeros if data is empty
                } else {
                    chartSeries.data
                }
                
                lineSeries {
                    series(data)
                }
            }
            
            // Store the date mapping for x-axis formatting
            extras { extraStore ->
                extraStore[xToDateMapKey] = xToDates
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, value, _ -> 
                    value.toInt().toString() 
                },
                label = rememberAxisLabelComponent(),
                guideline = rememberAxisGuidelineComponent()
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { context, x, _ ->
                    val dateMap = context.model.extraStore.getOrNull(xToDateMapKey)
                    val date = dateMap?.get(x.toFloat()) ?: weekStart.plus(DatePeriod(days = x.toInt()))
                    date.dayOfWeek.name.take(3) // Show abbreviated day name
                },
                label = rememberAxisLabelComponent(),
                guideline = rememberAxisGuidelineComponent()
            )
        ),
        modelProducer = cartesianChartModelProducer,
        modifier = modifier
    )
} 