package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.MR
import com.esteban.ruano.lifecommander.models.finance.SortOrder
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat
import com.lifecommander.finance.model.*
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScheduledTransactionList(
    transactions: List<ScheduledTransaction>,
    totalCount: Long,
    onTransactionClick: (ScheduledTransaction) -> Unit,
    onAddTransaction: () -> Unit,
    onEdit: (ScheduledTransaction) -> Unit,
    onDelete: (ScheduledTransaction) -> Unit,
    onLoadMore: () -> Unit,
    onFiltersChange: (TransactionFilters) -> Unit,
    currentFilters: TransactionFilters,
    onShowFilters: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Handle pagination
    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                println("TRIGGERING LAUNCHED EFFECT")
                println("$lastVisibleItemIndex")
                println("${transactions.size}")
                println(transactions)
                if (listState.layoutInfo.totalItemsCount>0 && (lastVisibleItemIndex?:0)>0 && lastVisibleItemIndex != null && lastVisibleItemIndex >= listState.layoutInfo.totalItemsCount - 5) {
                    println("LOADING MORE lastVisibleItem $lastVisibleItemIndex")
                    onLoadMore()
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Search and Filter Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                color = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentFilters.searchPattern ?: "",
                            onValueChange = { 
                                onFiltersChange(currentFilters.copy(searchPattern = it.takeIf { it.isNotBlank() }))
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(MR.strings.search_transactions)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = MaterialTheme.colors.surface
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))


                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                val newSortOrder = when (currentFilters.amountSortOrder) {
                                    SortOrder.NONE -> SortOrder.ASCENDING
                                    SortOrder.ASCENDING -> SortOrder.DESCENDING
                                    SortOrder.DESCENDING -> SortOrder.NONE
                                }
                                onFiltersChange(currentFilters.copy(amountSortOrder = newSortOrder))
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (currentFilters.amountSortOrder != SortOrder.NONE)
                                    MaterialTheme.colors.primary
                                else
                                    MaterialTheme.colors.surface,
                            )
                        ) {
                            Icon(
                                imageVector = when (currentFilters.amountSortOrder) {
                                    SortOrder.ASCENDING -> Icons.Default.ArrowUpward
                                    SortOrder.DESCENDING -> Icons.Default.ArrowDownward
                                    SortOrder.NONE -> Icons.Default.Sort
                                },
                                contentDescription = "Sort by amount",
                                tint = when (currentFilters.amountSortOrder) {
                                    SortOrder.NONE -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colors.onPrimary
                                }
                            )
                        }

                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = { onShowFilters(true) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (currentFilters != TransactionFilters()) 
                                    MaterialTheme.colors.primary 
                                else 
                                    MaterialTheme.colors.surface,
                                contentColor = if (currentFilters != TransactionFilters()) 
                                    MaterialTheme.colors.onPrimary 
                                else 
                                    MaterialTheme.colors.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = stringResource(MR.strings.filter_transactions)
                            )
                        }

                        if (currentFilters != TransactionFilters()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onFiltersChange(TransactionFilters()) }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(MR.strings.clear_filters),
                                    tint = MaterialTheme.colors.error
                                )
                            }
                        }
                    }
                }
            }

            // Transaction List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(bottom = 110.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        elevation = 2.dp,
                        onClick = onAddTransaction
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(MR.strings.add_scheduled_transaction),
                                tint = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Scheduled Transaction",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
                items(transactions) { transaction ->
                    ScheduledTransactionItem(
                        transaction = transaction,
                        onTransactionClick = onTransactionClick,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }

                if (transactions.size < totalCount) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Income Summary
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Total Income",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = transactions
                                .filter { it.type == TransactionType.INCOME }
                                .sumOf { it.amount }
                                .toCurrencyFormat(),
                            style = MaterialTheme.typography.h6,
                            color = Color.Green
                        )
                    }

                    // Expense Summary
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = transactions
                                .filter { it.type == TransactionType.EXPENSE }
                                .sumOf { it.amount }
                                .toCurrencyFormat(),
                            style = MaterialTheme.typography.h6,
                            color = Color.Red
                        )
                    }

                    // Net Summary
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Net Amount",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        val netAmount = transactions.sumOf { 
                            when (it.type) {
                                TransactionType.INCOME -> it.amount
                                TransactionType.EXPENSE -> -it.amount
                                TransactionType.TRANSFER -> 0.0
                            }
                        }
                        Text(
                            text = netAmount.toCurrencyFormat(),
                            style = MaterialTheme.typography.h6,
                            color = if (netAmount >= 0) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScheduledTransactionItem(
    transaction: ScheduledTransaction,
    onTransactionClick: (ScheduledTransaction) -> Unit,
    onEdit: (ScheduledTransaction) -> Unit,
    onDelete: (ScheduledTransaction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = 2.dp,
        onClick = { onTransactionClick(transaction) }
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Main Content Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Description and Category
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (transaction.type) {
                                    TransactionType.INCOME -> Icons.Outlined.TrendingUp
                                    TransactionType.EXPENSE -> Icons.Outlined.TrendingDown
                                    TransactionType.TRANSFER -> Icons.Outlined.SwapHoriz
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (transaction.type) {
                                    TransactionType.INCOME -> Color.Green
                                    TransactionType.EXPENSE -> Color.Red
                                    TransactionType.TRANSFER -> Color.Blue
                                }
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = transaction.category.name.replace("_", " ").lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Right Column: Amount
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = transaction.amount.toCurrencyFormat(),
                            style = MaterialTheme.typography.subtitle1,
                            color = when (transaction.type) {
                                TransactionType.INCOME -> Color.Green
                                TransactionType.EXPENSE -> Color.Red
                                TransactionType.TRANSFER -> Color.Blue
                            }
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                )

                // Schedule Information Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Frequency and Interval
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${
                                transaction.frequency?.lowercase()?.replaceFirstChar { it.uppercase() }
                            } (${transaction.interval})",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Right: Start Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Starts: ${transaction.startDate}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onEdit(transaction) },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = { onDelete(transaction) },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Delete",
                            color = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}