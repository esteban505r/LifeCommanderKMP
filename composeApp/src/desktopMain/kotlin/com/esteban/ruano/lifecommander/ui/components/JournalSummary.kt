package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifecommander.models.dashboard.JournalEntryDTO

@Composable
fun JournalSummary(
    journalCompleted: Boolean,
    journalStreak: Int,
    recentJournalEntries: List<JournalEntryDTO>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colors.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (journalCompleted) "Journal Completed" else "Journal Pending",
                    style = MaterialTheme.typography.h6,
                    color = if (journalCompleted) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (journalCompleted) Color(0xFF4CAF50) else Color(0xFFF44336))
            }
            Divider()
            Text("Streak: $journalStreak days", style = MaterialTheme.typography.body2)
            Text("Recent Entries", style = MaterialTheme.typography.caption)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                recentJournalEntries.take(3).forEach { entry ->
                    Text("${entry.date}: ${entry.summary}", style = MaterialTheme.typography.body2)
                }
            }
        }
    }
} 