package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.theme.*

@Composable
fun TimerFormModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernModal(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = if (name.isEmpty()) "New Timer" else "Edit Timer",
        modifier = modifier,
        actions = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank() && durationMinutes in 1..1440
            ) {
                Text("Save")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Timer name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = DividerColor
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            OutlinedTextField(
                value = durationMinutes.toString(),
                onValueChange = { 
                    val newValue = it.toIntOrNull() ?: 0
                    if (newValue in 1..1440) { // 24 hours max
                        onDurationChange(newValue)
                    }
                },
                placeholder = { Text("Duration in minutes") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = DividerColor
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Text(
                text = "Duration must be between 1 and 1440 minutes (24 hours)",
                style = MaterialTheme.typography.caption,
                color = TextSecondary
            )
        }
    }
} 