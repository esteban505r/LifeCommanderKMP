package com.esteban.ruano.lifecommander.ui.screens

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
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.models.StudyItem
import com.lifecommander.models.StudySession
import com.lifecommander.models.StudyTopic
import kotlinx.datetime.TimeZone
import ui.components.ModernModal
import ui.components.StudyItemFormModal
import ui.components.StudySessionFormModal
import ui.components.StudyTopicFormModal

enum class StudyViewMode {
    TODAY,
    PIPELINE,
    SESSIONS
}

@Composable
fun StudyScreen(
    topics: List<StudyTopic>,
    items: List<StudyItem>,
    sessions: List<StudySession>,
    disciplines: List<String> = emptyList(),
    topicsLoading: Boolean,
    itemsLoading: Boolean,
    sessionsLoading: Boolean,
    onReload: () -> Unit,
    onCreateTopic: (StudyTopic) -> Unit,
    onUpdateTopic: (String, StudyTopic) -> Unit,
    onDeleteTopic: (String) -> Unit,
    onCreateItem: (StudyItem) -> Unit,
    onUpdateItem: (String, StudyItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onCreateSession: (StudySession) -> Unit,
    onUpdateSession: (String, StudySession) -> Unit,
    onCompleteSession: (String, String, String?) -> Unit,
    onDeleteSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(StudyViewMode.TODAY) }
    var showTopicModal by remember { mutableStateOf(false) }
    var showItemModal by remember { mutableStateOf(false) }
    var showSessionModal by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<StudyTopic?>(null) }
    var itemToEdit by remember { mutableStateOf<StudyItem?>(null) }
    var sessionToEdit by remember { mutableStateOf<StudySession?>(null) }
    
    val currentDate = getCurrentDateTime(TimeZone.currentSystemDefault()).date
    val todayItems = items.filter { 
        it.stage == "PENDING" || it.stage == "IN_PROGRESS" 
    }.take(5)
    val activeTopics = topics.filter { it.isActive }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Study",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentDate.formatDefault(),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showSessionModal = true },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Session")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Session")
                }
            }
        }

        // View Mode Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudyTabButton(
                text = "Today",
                isSelected = viewMode == StudyViewMode.TODAY,
                onClick = { viewMode = StudyViewMode.TODAY }
            )
            StudyTabButton(
                text = "Pipeline",
                isSelected = viewMode == StudyViewMode.PIPELINE,
                onClick = { viewMode = StudyViewMode.PIPELINE }
            )
            StudyTabButton(
                text = "Sessions",
                isSelected = viewMode == StudyViewMode.SESSIONS,
                onClick = { viewMode = StudyViewMode.SESSIONS }
            )
        }

        // Content based on view mode
        when (viewMode) {
            StudyViewMode.TODAY -> {
                TodayView(
                    topics = activeTopics,
                    items = todayItems,
                    itemsLoading = itemsLoading,
                    onCreateItem = { showItemModal = true },
                    onItemClick = { itemToEdit = it; showItemModal = true }
                )
            }
            StudyViewMode.PIPELINE -> {
                PipelineView(
                    items = items,
                    itemsLoading = itemsLoading,
                    topics = topics,
                    onCreateItem = { showItemModal = true },
                    onItemClick = { itemToEdit = it; showItemModal = true },
                    onDeleteItem = onDeleteItem
                )
            }
            StudyViewMode.SESSIONS -> {
                SessionsView(
                    sessions = sessions,
                    sessionsLoading = sessionsLoading,
                    topics = topics,
                    items = items,
                    onSessionClick = { sessionToEdit = it; showSessionModal = true },
                    onDeleteSession = onDeleteSession
                )
            }
        }
    }

    // Modals
    StudyTopicFormModal(
        isVisible = showTopicModal,
        onDismiss = { 
            showTopicModal = false
            topicToEdit = null
        },
        disciplines = disciplines,
        topic = topicToEdit,
        topics = topics,
        onSave = { topic ->
            if (topicToEdit != null) {
                onUpdateTopic(topicToEdit!!.id, topic)
            } else {
                onCreateTopic(topic)
            }
            showTopicModal = false
            topicToEdit = null
        }
    )

    StudyItemFormModal(
        isVisible = showItemModal,
        onDismiss = { 
            showItemModal = false
            itemToEdit = null
        },
        item = itemToEdit,
        topics = topics,
        onSave = { item ->
            if (itemToEdit != null) {
                onUpdateItem(itemToEdit!!.id, item)
            } else {
                onCreateItem(item)
            }
            showItemModal = false
            itemToEdit = null
        }
    )

    StudySessionFormModal(
        isVisible = showSessionModal,
        onDismiss = { 
            showSessionModal = false
            sessionToEdit = null
        },
        session = sessionToEdit,
        topics = topics,
        items = items,
        onSave = { session ->
            if (sessionToEdit != null) {
                onUpdateSession(sessionToEdit!!.id, session)
            } else {
                onCreateSession(session)
            }
            showSessionModal = false
            sessionToEdit = null
        },
        onComplete = { sessionId, actualEnd, notes ->
            onCompleteSession(sessionId, actualEnd, notes)
            showSessionModal = false
            sessionToEdit = null
        }
    )
}

@Composable
private fun StudyTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) 
                MaterialTheme.colors.primary 
            else 
                MaterialTheme.colors.surface
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun TodayView(
    topics: List<StudyTopic>,
    items: List<StudyItem>,
    itemsLoading: Boolean,
    onCreateItem: () -> Unit,
    onItemClick: (StudyItem) -> Unit
) {
    Column {
        Text(
            text = "Today's Focus",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (itemsLoading) {
            CircularProgressIndicator()
        } else if (items.isEmpty()) {
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
                items(items) { item ->
                    StudyItemCard(
                        item = item,
                        topic = item.topic, // Use topic object directly
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineView(
    items: List<StudyItem>,
    itemsLoading: Boolean,
    topics: List<StudyTopic>,
    onCreateItem: () -> Unit,
    onItemClick: (StudyItem) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pending Column
        PipelineColumn(
            title = "Pending",
            items = items.filter { it.stage == "PENDING" },
            topics = topics,
            onCreateItem = onCreateItem,
            onItemClick = onItemClick,
            onDeleteItem = onDeleteItem,
            modifier = Modifier.weight(1f)
        )

        // In Progress Column
        PipelineColumn(
            title = "In Progress",
            items = items.filter { it.stage == "IN_PROGRESS" },
            topics = topics,
            onCreateItem = onCreateItem,
            onItemClick = onItemClick,
            onDeleteItem = onDeleteItem,
            modifier = Modifier.weight(1f)
        )

        // Processed Column
        PipelineColumn(
            title = "Processed",
            items = items.filter { it.stage == "PROCESSED" },
            topics = topics,
            onCreateItem = onCreateItem,
            onItemClick = onItemClick,
            onDeleteItem = onDeleteItem,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PipelineColumn(
    title: String,
    items: List<StudyItem>,
    topics: List<StudyTopic>,
    onCreateItem: () -> Unit,
    onItemClick: (StudyItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    StudyItemCard(
                        item = item,
                        topic = item.topic, // Use topic object directly
                        onClick = { onItemClick(item) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionsView(
    sessions: List<StudySession>,
    sessionsLoading: Boolean,
    topics: List<StudyTopic>,
    items: List<StudyItem>,
    onSessionClick: (StudySession) -> Unit,
    onDeleteSession: (String) -> Unit
) {
    Column {
        Text(
            text = "Recent Sessions",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sessionsLoading) {
            CircularProgressIndicator()
        } else if (sessions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sessions yet",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    StudySessionCard(
                        session = session,
                        topic = session.topic, // Use topic object directly
                        item = session.studyItem, // Use studyItem object directly
                        onClick = { onSessionClick(session) },
                        onDelete = { onDeleteSession(session.id) }
                    )
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
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                onDelete?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
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
                    .padding(vertical = 8.dp)
            )
            
            Text(
                text = "Progress: ${item.progress}%",
                style = MaterialTheme.typography.caption
            )
            
            item.obsidianPath?.let {
                Text(
                    text = "Path: $it",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StudySessionCard(
    session: StudySession,
    topic: StudyTopic?,
    item: StudyItem?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.mode,
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
                item?.let {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.caption
                    )
                }
                session.durationMinutes?.let {
                    Text(
                        text = "Duration: ${it} min",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

