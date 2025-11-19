package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.TagChip
import com.esteban.ruano.lifecommander.utils.UiUtils.getColorByPriority
import com.esteban.ruano.lifecommander.utils.UiUtils.getIconByPriority
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalTime
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task
import kotlinx.datetime.toJavaLocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun TaskDetailScreen(
    task: Task,
    onNavigateBack: () -> Unit,
    onEditTask: (String) -> Unit,
    onCompleteTask: () -> Unit,
    onUncompleteTask: () -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            color = MaterialTheme.colors.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = task.name,
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getTaskStatus(task),
                            style = MaterialTheme.typography.subtitle2,
                            color = getStatusColor(task)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { onEditTask(task.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Priority and Completion Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    backgroundColor = getColorByPriority(task.priority).copy(alpha = 0.1f),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getIconByPriority(task.priority),
                            contentDescription = "Priority",
                            tint = getColorByPriority(task.priority),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = getPriorityText(task.priority),
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = getColorByPriority(task.priority)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Card(
                    modifier = Modifier.weight(1f),
                    backgroundColor = if (task.done == true) 
                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    else 
                        MaterialTheme.colors.surface,
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (task.done == true) "Completed" else "Pending",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = if (task.done == true) 
                                MaterialTheme.colors.primary 
                            else 
                                MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
            
            // Due Date Information
            if (task.dueDateTime != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Due Date",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = task.dueDateTime?.toLocalDateTime()?.formatDefault() ?: "",
                            style = MaterialTheme.typography.body1
                        )
                        
                        val daysRemaining = getDaysRemaining(task.dueDateTime)
                        if (daysRemaining != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = daysRemaining,
                                style = MaterialTheme.typography.caption,
                                color = getDaysRemainingColor(task.dueDateTime)
                            )
                        }
                    }
                }
            }
            
            // Scheduled Date Information
            if (task.scheduledDateTime != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = "Scheduled Date",
                                tint = MaterialTheme.colors.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Scheduled Date",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = task.scheduledDateTime?.toLocalDateTime()?.formatDefault() ?: "",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
            
            // Tags Section
            if (!task.tags.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Label,
                                contentDescription = "Tags",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(task.tags ?: emptyList()) { tag ->
                                TagChip(
                                    tag = tag,
                                    onClick = { /* Could navigate to tag detail in future */ },
                                    selected = false
                                )
                            }
                        }
                    }
                }
            }
            
            // Note Section
            if (!task.note.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Note,
                                contentDescription = "Note",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Note",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = task.note ?: "",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
            
            // Reminders Section
            if (!task.reminders.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Reminders",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Reminders (${task.reminders?.size ?: 0})",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        task.reminders?.forEach { reminder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Reminder",
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = reminder.time.toString(),
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = if (task.done == true) onUncompleteTask else onCompleteTask,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (task.done == true) 
                            MaterialTheme.colors.secondary 
                        else 
                            MaterialTheme.colors.primary
                    )
                ) {
                    Icon(
                        if (task.done == true) Icons.Default.Refresh else Icons.Default.Check,
                        contentDescription = if (task.done == true) "Mark as Pending" else "Mark as Complete"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (task.done == true) "Mark as Pending" else "Mark as Complete"
                    )
                }
                
                OutlinedButton(
                    onClick = { onEditTask(task.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Task")
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${task.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTask()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getPriorityText(priority: Int): String {
    return when (priority) {
        0 -> "Low"
        1 -> "Medium"
        2 -> "High"
        3 -> "Urgent"
        else -> "Low"
    }
}


private fun getTaskStatus(task: Task): String {
    return when {
        task.done == true -> "Completed"
        task.dueDateTime == null -> "No due date"
        else -> "Pending"
    }
}

@Composable
private fun getStatusColor(task: Task): Color {
    return when {
        task.done == true -> MaterialTheme.colors.primary
        task.dueDateTime == null -> Color.Gray
        else -> Color(0xFFFF9800) // Orange
    }
}

private fun getDaysRemaining(dueDateTime: String?): String? {
    if (dueDateTime == null) return null
    
    val dueDate = dueDateTime.toLocalDateTime().date
    val today = LocalDateTime.now().toLocalDate()

    val daysDiff = ChronoUnit.DAYS.between(today, dueDate.toJavaLocalDate())
    
    return when {
        daysDiff < 0 -> "${Math.abs(daysDiff)} days overdue"
        daysDiff == 0L -> "Due today"
        daysDiff == 1L -> "Due tomorrow"
        else -> "$daysDiff days remaining"
    }
}

private fun getDaysRemainingColor(dueDateTime: String?): Color {
    if (dueDateTime == null) return Color.Gray
    
    val dueDate = dueDateTime.toLocalDateTime().date
    val today = LocalDateTime.now().toLocalDate()
    
    if (dueDate == null) return Color.Gray
    
    val daysDiff = ChronoUnit.DAYS.between(today, dueDate.toJavaLocalDate())
    
    return when {
        daysDiff < 0 -> Color.Red
        daysDiff == 0L -> Color(0xFFFF9800) // Orange
        daysDiff <= 3L -> Color(0xFFFF9800) // Orange
        else -> Color.Gray
    }
} 