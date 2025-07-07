package ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.lifecommander.models.Reminder
import java.awt.Dimension

enum class ReminderType(val time: Long, val label: String) {
    FIFTEEN_MINUTES(15 * 60 * 1000L, "15 minutes"),
    ONE_HOUR(60 * 60 * 1000L, "1 hour"),
    EIGHT_HOURS(8 * 60 * 60 * 1000L, "8 hours"),
    ONE_DAY(24 * 60 * 60 * 1000L, "1 day")
}

@Composable
fun DesktopRemindersDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    if (show) {
        var selectedReminderType by remember { mutableStateOf(ReminderType.FIFTEEN_MINUTES) }
        
        DialogWindow(
            onCloseRequest = onDismiss,
            state = rememberDialogState(position = WindowPosition(Alignment.Center))
        ) {
            LaunchedEffect(Unit) {
                window.size = Dimension(400, 350)
            }
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add Reminder",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface
                            )
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Choose when you'd like to be reminded",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Reminder Options
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ReminderType.values()) { reminderType ->
                                ReminderOptionItem(
                                    reminderType = reminderType,
                                    isSelected = selectedReminderType == reminderType,
                                    onSelected = { selectedReminderType = reminderType }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    onConfirm(selectedReminderType.time)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Reminder")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderOptionItem(
    reminderType: ReminderType,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        backgroundColor = if (isSelected) 
            MaterialTheme.colors.primary.copy(alpha = 0.1f) 
        else 
            MaterialTheme.colors.surface,
        elevation = if (isSelected) 2.dp else 0.dp,
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colors.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Reminder",
                tint = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = reminderType.label,
                style = MaterialTheme.typography.body1,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Reminder",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = formatReminderTime(reminder.time),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete reminder",
                    tint = MaterialTheme.colors.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatReminderTime(timeInMillis: Long): String {
    return when (timeInMillis) {
        15 * 60 * 1000L -> "15 minutes before"
        60 * 60 * 1000L -> "1 hour before"
        8 * 60 * 60 * 1000L -> "8 hours before"
        24 * 60 * 60 * 1000L -> "1 day before"
        else -> "${timeInMillis / (60 * 1000)} minutes before"
    }
} 