package com.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.finance.ui.components.FormattedAmountInput
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.lifecommander.models.Frequency
import com.lifecommander.ui.components.CustomDatePicker

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetForm(
    initialBudget: Budget? = null,
    onSave: (Budget) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(initialBudget?.name ?: "") }
    var amount by remember { mutableStateOf(initialBudget?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initialBudget?.category ?: Category.OTHER) }
    var startDate by remember {
        mutableStateOf(
            initialBudget?.startDate?.toLocalDate()
        )
    }
    var endDate by remember {
        mutableStateOf(
            initialBudget?.endDate?.toLocalDate()
        )
    }

    var rollover by remember { mutableStateOf(initialBudget?.rollover ?: false) }
    var rolloverAmount by remember { mutableStateOf(initialBudget?.rolloverAmount?.toString() ?: "0.0") }
    var showDatePicker by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(initialBudget?.frequency ?: Frequency.MONTHLY) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingStartDate by remember { mutableStateOf(true) }


    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false },
        )
        {
            Surface {
                CustomDatePicker(
                    selectedDate = ( if (editingStartDate) startDate else endDate ) ?: getCurrentDateTime().date,
                    onDateSelected = {
                        if (
                            editingStartDate
                        ) {
                            startDate = it
                        } else {
                            endDate = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = {
                        showDatePicker = false
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

        errorMessage?.let {
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
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                cursorColor = MaterialTheme.colors.primary,
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        )

        FormattedAmountInput(
            amount = amount,
            onAmountChange = { amount = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Category", style = MaterialTheme.typography.body1)

        EnumChipSelector(
            enumValues = Category.entries.toTypedArray(),
            selectedValue = category,
            onValueSelected = { category = it ?: Category.OTHER },
            //  modifier = Modifier.fillMaxWidth(),
            labelMapper = { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
        )


        Text("Frequency", style = MaterialTheme.typography.body1)

        EnumChipSelector(
            enumValues = Frequency.entries.toTypedArray(),
            selectedValue = frequency,
            onValueSelected = { frequency = it ?: Frequency.MONTHLY },
            // modifier = Modifier.fillMaxWidth(),
            labelMapper = { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
        )


        Text("Start Date", style = MaterialTheme.typography.body1)

        OutlinedButton(
            onClick = {
                showDatePicker = true
            },
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = startDate?.formatDefault() ?: "Select a start date",
                style = MaterialTheme.typography.body1
            )
        }

        if(frequency == Frequency.ONE_TIME) {
            Text("End Date", style = MaterialTheme.typography.body1)

            OutlinedButton(
                onClick = {
                    editingStartDate = false
                    showDatePicker = true
                },
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = endDate?.formatDefault() ?: "Select an end date",
                    style = MaterialTheme.typography.body1
                )
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
                singleLine = true,
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
                    if (startDate == null) {
                        errorMessage = "Please select a start date"
                        return@Button
                    }

                    val budget = Budget(
                        name = name,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        startDate = startDate!!.formatDefault(),
                        frequency = frequency,
                        rolloverAmount = rolloverAmount.toDoubleOrNull() ?: 0.0,
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