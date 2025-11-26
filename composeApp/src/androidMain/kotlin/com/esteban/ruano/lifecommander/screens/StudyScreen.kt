package com.esteban.ruano.lifecommander.screens

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
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.models.StudyItem
import com.lifecommander.models.StudySession
import com.lifecommander.models.StudyTopic
import kotlinx.datetime.TimeZone

/**
 * Simplified Study Screen for Mobile
 * Provides quick access to start study sessions and view today's focus
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudyScreen(
    topics: List<StudyTopic> = emptyList(),
    items: List<StudyItem> = emptyList(),
    sessions: List<StudySession> = emptyList(),
    isLoading: Boolean = false,
    onStartSession: (String, String?, String?) -> Unit = { _, _, _ -> },
    onCompleteSession: (String, String, String?) -> Unit = { _, _, _ -> },
    onCreateItem: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("INPUT") }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var activeSessionId by remember { mutableStateOf<String?>(null) }
    
    val todayItems = items.filter { 
        it.stage == "PENDING" || it.stage == "IN_PROGRESS" 
    }.take(3)
    val activeTopics = topics.filter { it.isActive }
    val activeSession = sessions.find { it.actualEnd == null }

    Scaffold(
        floatingActionButton = {
            if (activeSession == null) {
                FloatingActionButton(
                    onClick = {
                        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).formatDefault()
                        onStartSession(selectedMode, selectedTopicId, selectedItemId)
                    },
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Session")
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).formatDefault()
                        onCompleteSession(activeSession.id, now, null)
                    },
                    backgroundColor = MaterialTheme.colors.error
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop Session")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Study",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (activeSession != null) {
                // Active Session Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Active Session",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mode: ${activeSession.mode}",
                            style = MaterialTheme.typography.body2
                        )
                        activeSession.topic?.id?.let {
                            topics.find { t -> t.id == it }?.let { topic ->
                                Text(
                                    text = "Topic: ${topic.name}",
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }

            // Quick Start Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quick Start",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Mode Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("INPUT", "PROCESSING", "REVIEW").forEach { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(mode, style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                }
            }

            // Today's Focus
            Text(
                text = "Today's Focus",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (todayItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No items for today",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onCreateItem) {
                            Text("Create Study Item")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todayItems) { item ->
                        StudyItemCard(
                            item = item,
                            topic = topics.find { it.id == item.topic?.id },
                            onClick = { selectedItemId = item.id }
                        )
                    }
                }
            }

            // Recent Sessions
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recent Sessions",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (sessions.isEmpty()) {
                Text(
                    text = "No sessions yet",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions.take(5)) { session ->
                        StudySessionCard(
                            session = session,
                            topic = topics.find { it.id == session.topic?.id },
                            item = items.find { it.id == session.topic?.id }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StudyItemCard(
    item: StudyItem,
    topic: StudyTopic?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
            topic?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            LinearProgressIndicator(
                progress = item.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Text(
                text = "${item.progress}% - ${item.stage}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
private fun StudySessionCard(
    session: StudySession,
    topic: StudyTopic?,
    item: StudyItem?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.mode,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )
                topic?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.caption
                    )
                }
                session.durationMinutes?.let {
                    Text(
                        text = "${it} min",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

