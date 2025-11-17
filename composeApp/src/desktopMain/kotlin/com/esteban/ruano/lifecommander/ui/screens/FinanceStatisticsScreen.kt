package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.StatsChart
import com.esteban.ruano.lifecommander.ui.components.ChartSeries
import com.esteban.ruano.lifecommander.ui.components.BudgetColumnChart
import com.esteban.ruano.lifecommander.ui.viewmodels.*
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceStatisticsViewModel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.formatCurrency
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.*

@Composable
fun FinanceStatisticsScreen(
    statisticsViewModel: FinanceStatisticsViewModel,
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    budgetViewModel: BudgetViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statisticsState by statisticsViewModel.state.collectAsState()
    val transactionState by transactionViewModel.state.collectAsState()
    val accountState by accountViewModel.state.collectAsState()
    val budgetState by budgetViewModel.state.collectAsState()
    
    val statistics = statisticsState.statistics
    val transactions = transactionState.transactions
    val accounts = accountState.accounts
    val budgets = budgetState.budgets

    // Use backend statistics when available, otherwise calculate from local data
    val spendingPerDayThisWeek = statistics?.spendingPerDayThisWeek ?: remember(transactions.size) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        (0..6).map { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            transactions
                .filter { 
                    val transactionDate = try {
                        it.date.toLocalDateTime().date
                    } catch (e: Exception) {
                        it.date.toLocalDate()
                    }
                    transactionDate == date && it.amount < 0
                }
                .sumOf { -it.amount }.toInt()
        }
    }

    val incomePerDayThisWeek = statistics?.incomePerDayThisWeek ?: remember(transactions.size) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        (0..6).map { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            transactions
                .filter { 
                    val transactionDate = it.date.toLocalDateTime().date
                    transactionDate == date && it.amount > 0
                }
                .sumOf { it.amount }.toInt()
        }
    }

    val transactionsPerDayThisWeek = statistics?.transactionsPerDayThisWeek ?: remember(transactions.size) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        (0..6).map { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            transactions.count { 
                val transactionDate = try {
                    it.date.toLocalDateTime().date
                } catch (e: Exception) {
                    it.date.toLocalDate()
                }
                transactionDate == date
            }
        }
    }

    val spendingByCategory = statistics?.spendingByCategory?.toList()?.sortedByDescending { it.second }?.take(10)
         /*remember(transactions.size) {
            transactions
                .filter { it.amount < 0 && it.category != null }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> 
                    transactions.sumOf { -it.amount }
                }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
        }*/

    val totalBalance = statistics?.totalBalance ?: remember(accounts.size) {
        accounts.sumOf { it.balance ?: 0.0 }
    }

    val monthlyIncome = statistics?.monthlyIncome ?: remember(transactions.size) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfMonth = LocalDate(now.year, now.monthNumber, 1)
        val endOfMonth = startOfMonth.plus(DatePeriod(months = 1)).minus(1, DateTimeUnit.DAY)
        transactions
            .filter { 
                val transactionDate = try {
                    it.date.toLocalDateTime().date
                } catch (e: Exception) {
                    it.date.toLocalDate()
                }
                transactionDate >= startOfMonth && transactionDate <= endOfMonth && it.amount > 0
            }
            .sumOf { it.amount }
    }

    val monthlySpending = statistics?.monthlySpending ?: remember(transactions.size) {
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfMonth = LocalDate(now.year, now.monthNumber, 1)
        val endOfMonth = startOfMonth.plus(DatePeriod(months = 1)).minus(1, DateTimeUnit.DAY)
        transactions
            .filter { 
                val transactionDate = try {
                    it.date.toLocalDateTime().date
                } catch (e: Exception) {
                    it.date.toLocalDate()
                }
                transactionDate >= startOfMonth && transactionDate <= endOfMonth && it.amount < 0
            }
            .sumOf { -it.amount }
    }

    val savingsRate = statistics?.savingsRate ?: remember(monthlyIncome, monthlySpending) {
        if (monthlyIncome > 0) {
            ((monthlyIncome - monthlySpending) / monthlyIncome * 100).coerceAtLeast(0.0)
        } else {
            0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Finance Statistics",
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
                            "Financial Overview",
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
                                title = "Total Balance",
                                value = formatCurrency(totalBalance),
                                icon = Icons.Default.AccountBalance,
                                color = Color(0xFF2196F3)
                            )
                            StatCard(
                                title = "Monthly Income",
                                value = formatCurrency(monthlyIncome),
                                icon = Icons.Default.TrendingUp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatCard(
                                title = "Monthly Spending",
                                value = formatCurrency(monthlySpending),
                                icon = Icons.Default.TrendingDown,
                                color = Color(0xFFE53935)
                            )
                            StatCard(
                                title = "Savings Rate",
                                value = "${savingsRate.toInt()}%",
                                icon = Icons.Default.Savings,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
            }

            // Income vs Expenses Chart
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
                            "Income vs Expenses This Week",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsChart(
                            series = listOf(
                                ChartSeries(
                                    name = "Income",
                                    data = incomePerDayThisWeek,
                                    color = Color(0xFF4CAF50)
                                ),
                                ChartSeries(
                                    name = "Expenses",
                                    data = spendingPerDayThisWeek,
                                    color = Color(0xFFE53935)
                                )
                            ),
                            modifier = Modifier.height(250.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Transactions Count Chart
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
                            "Transactions This Week",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsChart(
                            series = listOf(
                                ChartSeries(
                                    name = "Transactions",
                                    data = transactionsPerDayThisWeek,
                                    color = Color(0xFF2196F3)
                                )
                            ),
                            modifier = Modifier.height(250.dp).fillMaxWidth()
                        )
                    }
                }
            }

            // Budget Progress Chart
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
                            budgets = budgets,
                            modifier = Modifier.height(350.dp).fillMaxWidth(),
                            onBudgetClick = { budgetProgress ->
                                println("Clicked on budget: ${budgetProgress.budget.name}")
                            }
                        )
                    }
                }
            }

            // Spending by Category
            if (spendingByCategory?.isNotEmpty() == true) {
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
                                "Top Spending Categories",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            spendingByCategory.forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                    Text(
                                        text = formatCurrency(amount),
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                            }
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

