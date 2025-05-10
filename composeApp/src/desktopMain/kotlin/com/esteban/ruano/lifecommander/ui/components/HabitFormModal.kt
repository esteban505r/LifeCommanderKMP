package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.theme.*

@Composable
fun HabitFormModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    scheduledTime: String?,
    onScheduledTimeChange: (String?) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernModal(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = if (name.isEmpty()) "New Habit" else "Edit Habit",
        modifier = modifier,
        actions =  {
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
                enabled = name.isNotBlank()
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
                placeholder = { Text("Habit name") },
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
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text("Notes (optional)") },
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
                value = scheduledTime ?: "",
                onValueChange = { onScheduledTimeChange(if (it.isEmpty()) null else it) },
                placeholder = { Text("Scheduled time (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = DividerColor
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
} 