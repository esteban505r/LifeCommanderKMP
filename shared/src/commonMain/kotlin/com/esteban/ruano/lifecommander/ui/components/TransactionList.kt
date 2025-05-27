package com.esteban.ruano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.MR
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.lifecommander.ui.components.ExpandableFilterSection
import com.esteban.ruano.lifecommander.ui.components.TransactionItem
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat
import com.lifecommander.finance.model.*
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.parseDate
import com.lifecommander.ui.components.CustomDatePicker
import com.lifecommander.ui.components.CustomTimePicker
import com.esteban.ruano.lifecommander.models.finance.SortOrder

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    totalCount: Long,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onLoadMore: () -> Unit,
    onFiltersChange: (TransactionFilters) -> Unit,
    currentFilters: TransactionFilters,
    onShowFilters: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Handle pagination
    LaunchedEffect(listState) {
        val lastItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
        if (lastItem != null && lastItem.index >= transactions.size - 5) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
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
                // Search and Filter Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Bar
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

                    Spacer(modifier = Modifier.width(16.dp))

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

                    // Clear Filters Button
                    if (currentFilters != TransactionFilters()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                onFiltersChange(TransactionFilters())
                            }
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
            modifier = Modifier.weight(1f),
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
                            contentDescription = stringResource(MR.strings.add_transaction),
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Transaction",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
            items(transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onTransactionClick = onTransactionClick,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            // Loading indicator at the bottom
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
}

