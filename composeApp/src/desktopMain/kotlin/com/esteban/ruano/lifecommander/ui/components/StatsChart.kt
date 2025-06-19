package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.shape.CorneredShape
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ChartSeries(
    val name: String,
    val data: List<Int> = emptyList(),
    val color: Color,
    val dateMap: Map<LocalDate, Int>? = null // If not null, use date-based X axis
)

private val xToDateMapKey = ExtraStore.Key<Map<Float, LocalDate>>()

private fun LocalDate.toEpochDay(): Long = this.toJavaLocalDate().toEpochDay()

@Composable
fun StatsChart(
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    val cartesianChartModelProducer = remember { CartesianChartModelProducer() }

    // If any series uses dateMap, use date-based X axis
    val isDateBased = series.any { it.dateMap != null }

    // Calculate the start of the current week
    val weekStart = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            .minus(DatePeriod(days = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.value - 1))
    }

    // For date-based: build x/y lists and xToDates map
    val xToDates: Map<Float, LocalDate> = if (isDateBased) {
        series.firstOrNull { it.dateMap != null }?.dateMap?.keys?.associateBy { it.toEpochDay().toFloat() } ?: emptyMap()
    } else emptyMap()

    val chartLines = series.map { chartSeries ->
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(chartSeries.color)),
            stroke = LineCartesianLayer.LineStroke.Continuous(),
            pointProvider = LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(
                    component = LineComponent(
                        fill = Fill(chartSeries.color),
                        thickness = 4.dp,
                        shape = CorneredShape(),
                        strokeThickness = 1.dp
                    ),
                    size = 8.dp
                )
            )
        )
    }

    LaunchedEffect(series) {
        cartesianChartModelProducer.runTransaction {
            if (isDateBased) {
                // Use date-based X axis
                val xVals = xToDates.keys.sorted()
                val yVals = xVals.map { x ->
                    val date = xToDates[x]!!
                    series.first { it.dateMap != null }.dateMap!![date]?.toFloat() ?: 0f
                }
                lineSeries {
                    series(xVals, yVals)
                }
                extras { it[xToDateMapKey] = xToDates }
            } else {
                // Use index-based X axis
                lineSeries {
                    series.forEach { chartSeries ->
                        series(chartSeries.data.ifEmpty { List(7) { 0 } })
                    }
                }
            }
        }
    }

    Column(modifier = modifier) {
        // Optional: Add a legend
        Row(modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)) {
            series.forEach { chartSeries ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(chartSeries.color, shape = CircleShape)
                    )
                    Text(chartSeries.name, color = Color.White, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(chartLines)
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = CartesianValueFormatter { _, value, _ ->
                        value.toInt().toString()
                    },
                    label = rememberAxisLabelComponent(
                        style = TextStyle(
                            color = Color.White,
                        )
                    ),
                    guideline = rememberAxisGuidelineComponent()
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { context, x, _ ->
                        val dateMap = context.model.extraStore.getOrNull(xToDateMapKey)
                        val date = dateMap?.get(x.toFloat()) ?: weekStart.plus(DatePeriod(days = x.toInt()))
                        date.dayOfWeek.name.take(3)
                    },
                    label = rememberAxisLabelComponent(
                        style = TextStyle(
                            color = Color.White,
                        )
                    ),
                    guideline = rememberAxisGuidelineComponent()
                )
            ),
            modelProducer = cartesianChartModelProducer,
            modifier = Modifier.fillMaxWidth().height(220.dp)
        )
    }
} 