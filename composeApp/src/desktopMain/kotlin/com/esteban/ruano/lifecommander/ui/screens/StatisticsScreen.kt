package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.StatsChart
import com.esteban.ruano.lifecommander.ui.components.ChartSeries
import com.esteban.ruano.lifecommander.ui.components.BudgetColumnChart
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.toJavaLocalDate
import ui.viewmodels.DashboardViewModel
import ui.viewmodels.HabitsViewModel
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt

@Composable
fun StatisticsScreen(
    dashboardViewModel: DashboardViewModel,
    financeViewModel: FinanceViewModel,
    timersViewModel: TimersViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pomodoros by timersViewModel.pomodoros.collectAsState()
    val budgets by financeViewModel.state.collectAsState()

    // Calculate pomodoros per day this week
    val pomodorosPerDayThisWeek = remember(pomodoros) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        val counts = MutableList(7) {
            startOfWeek.plus(DatePeriod(days = it))
        }
        counts.map { date ->
            pomodoros.count {
                it.startDateTime.toLocalDateTime().date == date
            }
        }
    }

    // Helper: Meals per day this week as dateMap
    val mealsPerDayDateMap = remember(dashboardViewModel.mealsLoggedPerDayThisWeek.collectAsState().value) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        (0..6).associate { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            date to (dashboardViewModel.mealsLoggedPerDayThisWeek.value.getOrNull(offset) ?: 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Statistics",
                        color = MaterialTheme.colors.onPrimary
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colors.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weekly Overview Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Weekly Overview",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsChart(
                            series = listOf(
                                ChartSeries(
                                    name = "Tasks",
                                    data = dashboardViewModel.tasksCompletedPerDayThisWeek.collectAsState().value,
                                    color = Color(0xFF2196F3)
                                ),
                                ChartSeries(
                                    name = "Habits",
                                    data = dashboardViewModel.habitsCompletedPerDayThisWeek.collectAsState().value,
                                    color = Color(0xFF9C27B0)
                                )
                            ),
                            modifier = Modifier.height(250.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Pomodoros Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Pomodoros This Week",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsChart(
                            series = listOf(
                                ChartSeries(
                                    name = "Pomodoros",
                                    data = pomodorosPerDayThisWeek,
                                    color = Color(0xFFE53935)
                                )
                            ),
                            modifier = Modifier.height(250.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Meals Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Meals This Week",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsChart(
                            series = listOf(
                                ChartSeries(
                                    name = "Planned Meals",
                                    data = dashboardViewModel.plannedMealsPerDayThisWeek.collectAsState().value,
                                    color = Color(0xFFFFA726)
                                ),
                                ChartSeries(
                                    name = "Unexpected Meals",
                                    data = dashboardViewModel.unexpectedMealsPerDayThisWeek.collectAsState().value,
                                    color = Color(0xFFFF5722)
                                )
                            ),
                            modifier = Modifier.height(250.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Workout Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Workouts This Week",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsChart(
                            series = listOf(
                                ChartSeries(
                                    name = "Workouts",
                                    data = dashboardViewModel.workoutsCompletedPerDayThisWeek.collectAsState().value,
                                    color = Color(0xFF4CAF50)
                                )
                            ),
                            modifier = Modifier.height(250.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Budget Chart (Column Chart)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Budget Progress",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BudgetColumnChart(
                            budgets = budgets.budgets,
                            modifier = Modifier.height(350.dp).fillMaxWidth(),
                            onBudgetClick = { budgetProgress ->
                                // Handle budget click - you can navigate to budget details or show more info
                                println("Clicked on budget: ${budgetProgress.budget.name}")
                            }
                        )
                    }
                }
            }

            // Summary Statistics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Weekly Summary",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatCard(
                                title = "Task Completion",
                                value = "${(dashboardViewModel.weeklyTaskCompletion.collectAsState().value * 100).toInt()}%",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF2196F3)
                            )
                            StatCard(
                                title = "Habit Completion",
                                value = "${(dashboardViewModel.weeklyHabitCompletion.collectAsState().value * 100).toInt()}%",
                                icon = Icons.Default.TrendingUp,
                                color = Color(0xFF9C27B0)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatCard(
                                title = "Workout Completion",
                                value = "${(dashboardViewModel.weeklyWorkoutCompletion.collectAsState().value * 100).toInt()}%",
                                icon = Icons.Default.FitnessCenter,
                                color = Color(0xFF4CAF50)
                            )
                            StatCard(
                                title = "Meal Logging",
                                value = "${(dashboardViewModel.weeklyMealLogging.collectAsState().value * 100).toInt()}%",
                                icon = Icons.Default.Restaurant,
                                color = Color(0xFFFFA726)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.weight(1f),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
} 