package com.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifecommander.finance.model.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetForm(
    initialBudget: Budget? = null,
    onSave: (Budget) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialBudget?.name ?: "") }
    var amount by remember { mutableStateOf(initialBudget?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initialBudget?.category ?: Category.OTHER) }
    var startDate by remember { mutableStateOf(initialBudget?.startDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }
    val endDate by remember {
        mutableStateOf(
            initialBudget?.endDate ?: Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .plus(DatePeriod(months = 1))
                .atTime(LocalTime(0, 0))
        )
    }
    var isRecurring by remember { mutableStateOf(initialBudget?.isRecurring ?: false) }
    var recurrence by remember { mutableStateOf(initialBudget?.recurrence ?: Recurrence.MONTHLY) }
    var rollover by remember { mutableStateOf(initialBudget?.rollover ?: false) }
    var rolloverAmount by remember { mutableStateOf(initialBudget?.rolloverAmount?.toString() ?: "0.0") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (initialBudget == null) "Add Budget" else "Edit Budget",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                cursorColor = MaterialTheme.colors.primary,
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                cursorColor = MaterialTheme.colors.primary,
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        )

        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = category.name,
                onValueChange = { },
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = startDate.toString(),
                onValueChange = { },
                readOnly = true,
                label = { Text("Start Date") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )
            OutlinedTextField(
                value = endDate.toString(),
                onValueChange = { },
                readOnly = true,
                label = { Text("End Date") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colors.primary,
                    uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            )
            Text(
                "Recurring Budget",
                color = MaterialTheme.colors.onSurface
            )
        }

        if (isRecurring) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Recurrence.entries.forEach { recurrenceType ->
                    FilterChip(
                        selected = recurrence == recurrenceType,
                        onClick = { recurrence = recurrenceType },
                        content = {
                            Text(
                                recurrenceType.name,
                                color = if (recurrence == recurrenceType)
                                    MaterialTheme.colors.onPrimary
                                else
                                    MaterialTheme.colors.onSurface
                            )
                        },

                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rollover,
                onCheckedChange = { rollover = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colors.primary,
                    uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            )
            Text(
                "Allow Rollover",
                color = MaterialTheme.colors.onSurface
            )
        }

        if (rollover) {
            OutlinedTextField(
                value = rolloverAmount,
                onValueChange = { rolloverAmount = it },
                label = { Text("Rollover Amount") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.primary
                )
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val budget = Budget(
                        name = name,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        isRecurring = isRecurring,
                        recurrence = if (isRecurring) recurrence else null,
                        rollover = rollover,
                        rolloverAmount = rolloverAmount.toDoubleOrNull() ?: 0.0
                    )
                    onSave(budget)
                },
                enabled = name.isNotEmpty() && amount.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text("Save")
            }
        }
    }
} 