package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.kizitonwose.calendar.core.minusDays
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import services.dailyjournal.models.MoodType
import services.dailyjournal.models.QuestionType
import java.awt.Dimension
import services.dailyjournal.models.DailyJournalResponse

@Composable
fun JournalHistoryScreen(
    journalEntries: List<DailyJournalResponse>,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Journal History",
                                style = MaterialTheme.typography.h4,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface
                            )
                            Text(
                                text = "Browse your past journal entries",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Date picker button
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colors.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select Date Range",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Range")
                    }
                }
                
                // Quick date navigation
                if (journalEntries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recent Entries",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(journalEntries) { entry ->
                            val date = entry.date.toLocalDate()
                            DateChip(
                                date = date,
                                isSelected = selectedDate == date,
                                onClick = {
                                    selectedDate = date
                                    onDateRangeSelected(date, date)
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Content
        if (selectedDate != null) {
            val selectedEntry = journalEntries.find { it.date.toLocalDate() == selectedDate }
            
            if (selectedEntry != null) {
                SelectedDateContent(
                    entry = selectedEntry,
                    onBackToList = { selectedDate = null }
                )
            } else {
                EmptyDateContent(
                    date = selectedDate!!,
                    onBackToList = { selectedDate = null }
                )
            }
        } else {
            // Show list of all entries
            JournalEntriesList(
                entries = journalEntries,
                onEntrySelected = { entry ->
                    val date = entry.date.toLocalDate()
                    selectedDate = date
                    onDateRangeSelected(date, date)
                }
            )
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateRangeSelected = { startDate, endDate ->
                onDateRangeSelected(startDate, endDate)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.surface
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colors.onSurface
    }
    
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        color = backgroundColor,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfWeek.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun JournalEntriesList(
    entries: List<DailyJournalResponse>,
    onEntrySelected: (DailyJournalResponse) -> Unit
) {
    if (entries.isEmpty()) {
        EmptyHistoryState()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { entry ->
                JournalEntryCard(
                    entry = entry,
                    onClick = { onEntrySelected(entry) }
                )
            }
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: DailyJournalResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date indicator
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = entry.date.toLocalDate().dayOfMonth.toString(),
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = entry.date.toLocalDate().month.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.date.toLocalDate().formatDefault(),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "${entry.questionAnswers.size} questions answered",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                // Show mood summary if available
                val moodAnswers = entry.questionAnswers.filter { it.type == QuestionType.MOOD }
                if (moodAnswers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mood: ",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        moodAnswers.forEach { question ->
                            val mood = try {
                                MoodType.valueOf(question.answer)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                            mood?.let {
                                Text(
                                    text = it.emoji,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SelectedDateContent(
    entry: DailyJournalResponse,
    onBackToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToList,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back to List",
                    tint = MaterialTheme.colors.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = entry.date.toLocalDate().formatDefault(),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Questions and answers
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(entry.questionAnswers) { question ->
                CompletedJournalCard(
                    question = question,
                    answer = question.answer
                )
            }
        }
    }
}

@Composable
fun EmptyDateContent(
    date: LocalDate,
    onBackToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "No Entry",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Journal Entry",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No journal entry was found for ${date.formatDefault()}",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onBackToList,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to History")
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "No History",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Journal History",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You haven't completed any journal entries yet. Start journaling to see your history here.",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf(getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ).date.minusDays(7)) }
    var endDate by remember { mutableStateOf(getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ).date) }
    
    DialogWindow(
        onCloseRequest = onDismiss,
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        LaunchedEffect(Unit) {
            window.size = Dimension(500, 600)
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select Date Range",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Date range display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "From ${startDate.formatDefault()}",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "To ${endDate.formatDefault()}",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(endDate.toEpochDays() - startDate.toEpochDays() + 1)} days",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick range selection
                Text(
                    text = "Quick Ranges",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    items(quickRanges) { range ->
                        QuickRangeButton(
                            range = range,
                            isSelected = startDate == range.start && endDate == range.end,
                            onClick = { 
                                startDate = range.start
                                endDate = range.end
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Manual date range input
                Text(
                    text = "Or enter specific dates",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startDate.formatDefault(),
                        onValueChange = { dateString ->
                            try {
                                val newDate = dateString.toLocalDate()
                                if (newDate <= endDate) {
                                    startDate = newDate
                                }
                            } catch (e: Exception) {
                                // Invalid date format, ignore
                            }
                        },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = MaterialTheme.colors.surface,
                            focusedIndicatorColor = MaterialTheme.colors.primary,
                            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                            cursorColor = MaterialTheme.colors.primary
                        )
                    )
                    
                    OutlinedTextField(
                        value = endDate.formatDefault(),
                        onValueChange = { dateString ->
                            try {
                                val newDate = dateString.toLocalDate()
                                if (newDate >= startDate) {
                                    endDate = newDate
                                }
                            } catch (e: Exception) {
                                // Invalid date format, ignore
                            }
                        },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = MaterialTheme.colors.surface,
                            focusedIndicatorColor = MaterialTheme.colors.primary,
                            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                            cursorColor = MaterialTheme.colors.primary
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onDateRangeSelected(startDate, endDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Select Range")
                    }
                }
            }
        }
    }
}

data class DateRange(
    val start: LocalDate,
    val end: LocalDate,
    val label: String
)

@Composable
fun QuickRangeButton(
    range: DateRange,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.surface
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colors.onSurface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = range.label,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "${range.start.formatDefault()} - ${range.end.formatDefault()}",
                style = MaterialTheme.typography.caption,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private val quickRanges = listOf(
    DateRange(
        start = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(7),
        end = getCurrentDateTime(TimeZone.currentSystemDefault()).date,
        label = "Last 7 Days"
    ),
    DateRange(
        start = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(30),
        end = getCurrentDateTime(TimeZone.currentSystemDefault()).date,
        label = "Last 30 Days"
    ),
    DateRange(
        start = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(90),
        end = getCurrentDateTime(TimeZone.currentSystemDefault()).date,
        label = "Last 3 Months"
    ),
    DateRange(
        start = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(180),
        end = getCurrentDateTime(TimeZone.currentSystemDefault()).date,
        label = "Last 6 Months"
    ),
    DateRange(
        start = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(365),
        end = getCurrentDateTime(TimeZone.currentSystemDefault()).date,
        label = "Last Year"
    ),
    DateRange(
        start = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(7),
        end = getCurrentDateTime(TimeZone.currentSystemDefault()).date.minusDays(1),
        label = "Previous Week"
    )
)