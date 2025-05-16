package com.lifecommander.finance.ui.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lifecommander.finance.model.Recurrence


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RecurrenceDropdown(
    selectedRecurrence: Recurrence?,
    onRecurrenceSelected: (Recurrence) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedRecurrence?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
            onValueChange = {},
            label = { Text("Recurrence") },
            isError = error != null,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Recurrence.entries.forEach { recurrence ->
                DropdownMenuItem(
                    text = { Text(recurrence.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onRecurrenceSelected(recurrence)
                        expanded = false
                    }
                )
            }
        }
    }

    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.body1
        )
    }
}
