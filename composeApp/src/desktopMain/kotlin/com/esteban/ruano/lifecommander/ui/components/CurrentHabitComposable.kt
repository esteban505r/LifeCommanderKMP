package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import services.habits.models.HabitResponse
import utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.format
import utils.DateUtils.toLocalTime
import utils.TimeBasedItemUtils
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CurrentHabitComposable(
    habits: List<HabitResponse>
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var habitTimeInfo by remember { mutableStateOf(TimeBasedItemUtils.calculateHabitTimes(habits, currentTime)) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(habits, currentTime) {
        habitTimeInfo = TimeBasedItemUtils.calculateHabitTimes(habits, currentTime)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Current Habit Section
            AnimatedVisibility(
                visible = habitTimeInfo.currentItem != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habitTimeInfo.currentItem?.name ?: "",
                                style = MaterialTheme.typography.h4,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        habitTimeInfo.timeRemaining?.let { duration ->
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = duration.format(),
                                    style = MaterialTheme.typography.h6,
                                    color = MaterialTheme.colors.primary
                                )
                                Text(
                                    text = "remaining",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Next Habit Section
            AnimatedVisibility(
                visible = habitTimeInfo.nextItem != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.secondary.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Next Habit",
                                style = MaterialTheme.typography.subtitle2,
                                color = MaterialTheme.colors.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = habitTimeInfo.nextItem?.name ?: "",
                                style = MaterialTheme.typography.h6,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        habitTimeInfo.nextItem?.dateTime?.let { time ->
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = time.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.h6,
                                    color = MaterialTheme.colors.secondary
                                )
                                Text(
                                    text = "starts at",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.secondary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
