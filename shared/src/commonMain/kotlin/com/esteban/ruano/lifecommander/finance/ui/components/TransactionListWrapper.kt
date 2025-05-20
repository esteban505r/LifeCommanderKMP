package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.finance.*
import com.esteban.ruano.lifecommander.ui.components.*
import com.esteban.ruano.ui.components.TransactionList
import com.lifecommander.finance.model.*
import com.lifecommander.ui.components.CustomDatePicker
import com.lifecommander.ui.components.CustomTimePicker
import kotlinx.datetime.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.parseDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionListWrapper(
    transactions: List<Transaction>,
    totalCount: Long,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onLoadMore: () -> Unit,
    onFiltersChange: (TransactionFilters) -> Unit,
    currentFilters: TransactionFilters
) {
    var showFilters by remember { mutableStateOf(false) }
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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        TransactionList(
            transactions = transactions,
            totalCount = totalCount,
            onTransactionClick = onTransactionClick,
            onAddTransaction = onAddTransaction,
            onEdit = onEdit,
            onDelete = onDelete,
            onLoadMore = onLoadMore,
            onFiltersChange = onFiltersChange,
            currentFilters = currentFilters,
            onShowFilters = { showFilters = it }
        )

        FilterSidePanel(
            isVisible = showFilters,
            onDismiss = { showFilters = false },
            onClearFilters = { 
                selectedCategories = emptyList()
                selectedTypes = emptyList()
                startDateTime = null
                endDateTime = null
                minAmount = ""
                maxAmount = ""
                onFiltersChange(TransactionFilters())
            },
            hasActiveFilters = currentFilters != TransactionFilters()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Types Section
                ExpandableFilterSection(
                    title = "Transaction Types",
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

                // Categories Section
                ExpandableFilterSection(
                    title = "Categories",
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

                // Date Range Section
                ExpandableFilterSection(
                    title = "Date Range",
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
                    } else null
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Date and Time
                        Text(
                            text = "Start Date",
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
                                        ?: "Select Date",
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
                                        ?: "Select Time",
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
                            text = "End Date",
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
                                        ?: "Select Date",
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
                                        ?: "Select Time",
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

                // Amount Range Section
                ExpandableFilterSection(
                    title = "Amount Range",
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
                            placeholder = { Text("Min Amount") },
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
                            placeholder = { Text("Max Amount") },
                            singleLine = true
                        )
                    }
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
} 