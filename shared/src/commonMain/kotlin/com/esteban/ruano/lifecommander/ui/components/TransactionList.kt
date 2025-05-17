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
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(currentFilters.searchPattern ?: "") }
    var selectedCategories by remember { mutableStateOf(currentFilters.categories ?: emptyList()) }
    var selectedTypes by remember { mutableStateOf(currentFilters.types ?: emptyList()) }
    var minAmount by remember { mutableStateOf(currentFilters.minAmount?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(currentFilters.maxAmount?.toString() ?: "") }
    var showstartDateTimePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showendDateTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var startDateTime by remember { 
        mutableStateOf(
            currentFilters.startDate?.toLocalDateTime()
        )
    }
    var endDateTime by remember { 
        mutableStateOf(
            currentFilters.endDate?.toLocalDateTime()
        )
    }

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
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
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

                    // Filter Toggle Button
                    Button(
                        onClick = { showFilters = !showFilters },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (showFilters) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                            contentColor = if (showFilters) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary
                        )
                    ) {
                        Icon(
                            if (showFilters) Icons.Default.FilterList else Icons.Default.FilterListOff,
                            contentDescription = stringResource(MR.strings.filter_transactions)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(MR.strings.filter_transactions))
                    }

                    // Clear Filters Button
                    if (currentFilters != TransactionFilters()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                searchQuery = ""
                                selectedCategories = emptyList()
                                selectedTypes = emptyList()
                                startDateTime = null
                                endDateTime = null
                                minAmount = ""
                                maxAmount = ""
                                onFiltersChange(TransactionFilters(
                                    searchPattern = null,
                                    categories = null,
                                    startDate = null,
                                    startDateHour = null,
                                    endDate = null,
                                    endDateHour = null,
                                    types = null,
                                    minAmount = null,
                                    maxAmount = null,
                                    accountIds = null
                                ))
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

                // Filter Panel
                if (showFilters) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        elevation = 0.dp,
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            // Transaction Types Section
                            ExpandableFilterSection(
                                title = stringResource(MR.strings.transaction_types),
                                summary = if (selectedTypes.isNotEmpty()) {
                                    selectedTypes.joinToString(", ")
                                } else null
                            ) {
                                EnumChipSelector(
                                    enumValues = TransactionType.entries.toTypedArray(),
                                    selectedValues = selectedTypes.toSet(),
                                    onValueSelected = { types ->
                                        selectedTypes = types.toList()
                                        onFiltersChange(currentFilters.copy(types = selectedTypes))
                                    },
                                    multiSelect = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Categories Section
                            ExpandableFilterSection(
                                title = stringResource(MR.strings.categories),
                                summary = if (selectedCategories.isNotEmpty()) {
                                    selectedCategories.joinToString(", ")
                                } else null
                            ) {
                                EnumChipSelector(
                                    enumValues = Category.entries.toTypedArray(),
                                    selectedValues = selectedCategories.map { Category.valueOf(it) }.toSet(),
                                    onValueSelected = { categories ->
                                        selectedCategories = categories.map { it.name }
                                        onFiltersChange(currentFilters.copy(categories = selectedCategories))
                                    },
                                    multiSelect = true,
                                    labelMapper = { it.name }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date Range Section
                            ExpandableFilterSection(
                                title = stringResource(MR.strings.date_range),
                                summary = if (startDateTime != null || endDateTime != null) {
                                    buildString {
                                        if (startDateTime != null) {
                                            append("From: ${startDateTime?.date?.parseDate()}")
                                            if (startDateTime?.time != null) {
                                                append(" ${startDateTime?.time?.formatDefault()}")
                                            }
                                        }
                                        if (startDateTime != null && endDateTime != null) {
                                            append(" to ")
                                        }
                                        if (endDateTime != null) {
                                            append("To: ${endDateTime?.date?.parseDate()}")
                                            if (endDateTime?.time != null) {
                                                append(" ${endDateTime?.time?.formatDefault()}")
                                            }
                                        }
                                    }
                                } else null,
                                trailingIcon = {
                                    if (startDateTime != null || endDateTime != null) {
                                        IconButton(
                                            onClick = {
                                                startDateTime = null
                                                endDateTime = null
                                                onFiltersChange(currentFilters.copy(
                                                    startDate = null,
                                                    startDateHour = null,
                                                    endDate = null,
                                                    endDateHour = null
                                                ))
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = stringResource(MR.strings.clear_dates),
                                                tint = MaterialTheme.colors.error
                                            )
                                        }
                                    }
                                }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Start Date and Time
                                    Text(
                                        text = stringResource(MR.strings.start_date),
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showstartDateTimePicker = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = startDateTime?.date?.parseDate() 
                                                    ?: stringResource(MR.strings.select_date),
                                                style = MaterialTheme.typography.body1,
                                                color = if (startDateTime == null) 
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                else 
                                                    MaterialTheme.colors.onSurface
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showStartTimePicker = true },
                                            modifier = Modifier.weight(1f),
                                            enabled = startDateTime != null
                                        ) {
                                            Text(
                                                text = startDateTime?.time?.formatDefault()
                                                    ?: stringResource(MR.strings.select_time),
                                                style = MaterialTheme.typography.body1,
                                                color = if (startDateTime == null) 
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                else 
                                                    MaterialTheme.colors.onSurface
                                            )
                                        }
                                    }

                                    // End Date and Time
                                    Text(
                                        text = stringResource(MR.strings.end_date),
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showendDateTimePicker = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = endDateTime?.date?.parseDate()
                                                    ?: stringResource(MR.strings.select_date),
                                                style = MaterialTheme.typography.body1,
                                                color = if (endDateTime == null) 
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                else 
                                                    MaterialTheme.colors.onSurface
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showEndTimePicker = true },
                                            modifier = Modifier.weight(1f),
                                            enabled = endDateTime != null
                                        ) {
                                            Text(
                                                text = endDateTime?.time?.formatDefault()
                                                    ?: stringResource(MR.strings.select_time),
                                                style = MaterialTheme.typography.body1,
                                                color = if (endDateTime == null) 
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                else 
                                                    MaterialTheme.colors.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Date Pickers
                            if (showstartDateTimePicker) {
                                Dialog(onDismissRequest = { showstartDateTimePicker = false }) {
                                    Surface {
                                        CustomDatePicker(
                                            selectedDate = startDateTime?.date ?: getCurrentDateTime().date,
                                            onDateSelected = { 
                                                startDateTime = it.atTime(startDateTime?.time ?: LocalTime(0, 0))
                                                onFiltersChange(currentFilters.copy(
                                                    startDate = startDateTime?.date?.formatDefault(),
                                                    startDateHour = startDateTime?.time?.formatDefault()
                                                ))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            onDismiss = { showstartDateTimePicker = false }
                                        )
                                    }
                                }
                            }

                            if (showStartTimePicker) {
                                Dialog(onDismissRequest = { showStartTimePicker = false }) {
                                    Surface {
                                        CustomTimePicker(
                                            selectedTime = startDateTime?.time ?: LocalTime(0, 0),
                                            onTimeSelected = { 
                                                startDateTime = startDateTime?.date?.atTime(it)
                                                onFiltersChange(currentFilters.copy(
                                                    startDate = startDateTime?.date?.formatDefault(),
                                                    startDateHour = startDateTime?.time?.formatDefault()
                                                ))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            onDismiss = { showStartTimePicker = false }
                                        )
                                    }
                                }
                            }

                            if (showendDateTimePicker) {
                                Dialog(onDismissRequest = { showendDateTimePicker = false }) {
                                    Surface {
                                        CustomDatePicker(
                                            selectedDate = endDateTime?.date ?: getCurrentDateTime().date,
                                            onDateSelected = { 
                                                endDateTime = it.atTime(endDateTime?.time ?: LocalTime(0, 0))
                                                onFiltersChange(currentFilters.copy(
                                                    endDate = endDateTime?.date?.formatDefault(),
                                                    endDateHour = endDateTime?.time?.formatDefault()
                                                ))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            onDismiss = { showendDateTimePicker = false }
                                        )
                                    }
                                }
                            }

                            if (showEndTimePicker) {
                                Dialog(onDismissRequest = { showEndTimePicker = false }) {
                                    Surface {
                                        CustomTimePicker(
                                            selectedTime = endDateTime?.time ?: LocalTime(0, 0),
                                            onTimeSelected = { 
                                                endDateTime = endDateTime?.date?.atTime(it)
                                                onFiltersChange(currentFilters.copy(
                                                    endDate = endDateTime?.date?.formatDefault(),
                                                    endDateHour = endDateTime?.time?.formatDefault()
                                                ))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            onDismiss = { showEndTimePicker = false }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Amount Range Section
                            ExpandableFilterSection(
                                title = stringResource(MR.strings.amount_range),
                                summary = if (minAmount.isNotEmpty() || maxAmount.isNotEmpty()) {
                                    buildString {
                                        if (minAmount.isNotEmpty()) {
                                            append("Min: $minAmount")
                                        }
                                        if (minAmount.isNotEmpty() && maxAmount.isNotEmpty()) {
                                            append(" to ")
                                        }
                                        if (maxAmount.isNotEmpty()) {
                                            append("Max: $maxAmount")
                                        }
                                    }
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedTextField(
                                        value = minAmount,
                                        onValueChange = { 
                                            minAmount = it
                                            onFiltersChange(currentFilters.copy(minAmount = it.toDoubleOrNull()))
                                        },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text(stringResource(MR.strings.min_amount)) },
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    OutlinedTextField(
                                        value = maxAmount,
                                        onValueChange = { 
                                            maxAmount = it
                                            onFiltersChange(currentFilters.copy(maxAmount = it.toDoubleOrNull()))
                                        },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text(stringResource(MR.strings.max_amount)) },
                                        singleLine = true
                                    )
                                }
                            }
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

        // Add Transaction FAB
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(MR.strings.add_transaction),
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onTransactionClick: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.category.name,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount.toCurrencyFormat(),
                    style = MaterialTheme.typography.subtitle1,
                    color = when (transaction.type) {
                        TransactionType.INCOME -> Color.Green
                        TransactionType.EXPENSE -> Color.Red
                        TransactionType.TRANSFER -> MaterialTheme.colors.onSurface
                    }
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Row {
                IconButton(onClick = { onEdit(transaction) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(MR.strings.edit),
                        tint = MaterialTheme.colors.primary
                    )
                }
                
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(MR.strings.delete),
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.padding(4.dp),
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        contentColor = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
    ) {
        TextButton(
            onClick = onClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
            )
        ) {
            label()
        }
    }
} 