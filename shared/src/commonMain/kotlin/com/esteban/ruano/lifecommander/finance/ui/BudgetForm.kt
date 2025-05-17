package com.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.parseDateTime
import com.lifecommander.finance.model.*
import com.lifecommander.models.Frequency
import com.lifecommander.ui.components.CustomDatePicker
import com.lifecommander.ui.components.CustomTimePicker
import kotlinx.datetime.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetForm(
    initialBudget: Budget? = null,
    onSave: (Budget) -> Unit,
    onCancel: () -> Unit,
    ChipWrapper: @Composable (content: @Composable () -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(initialBudget?.name ?: "") }
    var amount by remember { mutableStateOf(initialBudget?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initialBudget?.category ?: Category.OTHER) }
    var startDate by remember {
        mutableStateOf(
            initialBudget?.startDate?.toLocalDateTime()
        )
    }

    var isRecurring by remember { mutableStateOf(initialBudget?.isRecurring ?: false) }
    var recurrence by remember { mutableStateOf(initialBudget?.recurrence ?: Recurrence.MONTHLY) }
    var rollover by remember { mutableStateOf(initialBudget?.rollover ?: false) }
    var rolloverAmount by remember { mutableStateOf(initialBudget?.rolloverAmount?.toString() ?: "0.0") }
    var showDatePicker by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(initialBudget?.frequency ?: Frequency.MONTHLY) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false },
        )
        {
            Surface {
                CustomDatePicker(
                    selectedDate = startDate?.date ?: getCurrentDateTime().date,
                    onDateSelected = {
                        startDate = it.atTime(startDate?.time ?: getCurrentDateTime().time)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = {
                        showDatePicker = false
                        showTimePicker = true
                    }
                )
            }
        }
    }

    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
        )
        {
            Surface {
                CustomTimePicker(
                    selectedTime = startDate?.time ?: getCurrentDateTime().time,
                    onTimeSelected = {
                        startDate = startDate?.date?.atTime(it) ?: getCurrentDateTime().date.atTime(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = {
                        showTimePicker = false
                    }
                )
            }
        }
    }


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

        errorMessage?.let{
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error
            )
        }

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

        Text("Category", style = MaterialTheme.typography.body1)

        ChipWrapper {
            EnumChipSelector(
                enumValues = Category.entries.toTypedArray(),
                selectedValue = category,
                onValueSelected = { category = it },
                //  modifier = Modifier.fillMaxWidth(),
                labelMapper = { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
            )
        }


        Text("Frequency", style = MaterialTheme.typography.body1)

        ChipWrapper{
            EnumChipSelector(
                enumValues = Frequency.entries.toTypedArray(),
                selectedValue = frequency,
                onValueSelected = { frequency = it },
                // modifier = Modifier.fillMaxWidth(),
                labelMapper = { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
            )
        }



        OutlinedButton(
            onClick = {
                showDatePicker = true
            },
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = startDate?.date?.formatDefault() ?: "Select a start date",
                style = MaterialTheme.typography.body1
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
                    if(startDate == null) {
                        errorMessage = "Please select a start date"
                        return@Button
                    }

                    val budget = Budget(
                        name = name,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        startDate = startDate!!.formatDefault(),
                        isRecurring = isRecurring,
                        recurrence = if (isRecurring) recurrence else null,
                        rollover = rollover,
                        rolloverAmount = rolloverAmount.toDoubleOrNull() ?: 0.0,
                        frequency = frequency,
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