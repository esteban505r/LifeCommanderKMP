package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import androidx.compose.animation.core.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.sin
import kotlin.math.cos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalWindowInfo

data class FloatingHabit(
    val id: String,
    val emoji: String,
    val name: String,
    val duration: String? = null
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FloatingHabitsComposable(
    modifier: Modifier = Modifier,
    onHabitClick: (FloatingHabit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedHabit by remember { mutableStateOf<FloatingHabit?>(null) }
    var timeRemaining by remember { mutableStateOf(0) }
    var windowSize by remember { mutableStateOf(IntSize(0, 0)) }
    var modalSize by remember { mutableStateOf(IntSize(0, 0)) }
    var position by remember { mutableStateOf(IntOffset(0, 0)) }
    var velocity by remember { mutableStateOf(IntOffset(4, 4)) }

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    // Update window size when it changes
    LaunchedEffect(Unit) {
        while (true) {
            windowSize = windowInfo.containerSize
            delay(16) // ~60fps
        }
    }

    // Bouncing animation
    LaunchedEffect(selectedHabit, windowSize) {
        if (selectedHabit != null && windowSize.width > 0 && windowSize.height > 0) {
            // Start from a random position
            position = IntOffset(
                (windowSize.width - modalSize.width) / 2,
                (windowSize.height - modalSize.height) / 2
            )
            
            while (true) {
                position = IntOffset(
                    position.x + velocity.x,
                    position.y + velocity.y
                )

                // Bounce off right edge
                if (position.x + modalSize.width > windowSize.width) {
                    position = IntOffset(windowSize.width - modalSize.width, position.y)
                    velocity = IntOffset(-velocity.x, velocity.y)
                }
                // Bounce off left edge
                if (position.x < 0) {
                    position = IntOffset(0, position.y)
                    velocity = IntOffset(-velocity.x, velocity.y)
                }
                // Bounce off bottom edge
                if (position.y + modalSize.height > windowSize.height) {
                    position = IntOffset(position.x, windowSize.height - modalSize.height)
                    velocity = IntOffset(velocity.x, -velocity.y)
                }
                // Bounce off top edge
                if (position.y < 0) {
                    position = IntOffset(position.x, 0)
                    velocity = IntOffset(velocity.x, -velocity.y)
                }

                delay(16) // ~60fps
            }
        }
    }

    val floatingHabits = listOf(
        FloatingHabit("1", "🧘", "Stretch", "2 mins"),
        FloatingHabit("2", "💧", "Drink water"),
        FloatingHabit("3", "📝", "Write one journal line"),
        FloatingHabit("4", "🪥", "Clean up one surface"),
        FloatingHabit("5", "🎸", "Play one song")
    )

    // Timer effect for habits with duration
    LaunchedEffect(selectedHabit) {
        if (selectedHabit?.duration != null) {
            val durationInSeconds = selectedHabit!!.duration!!.split(" ")[0].toInt() * 60
            timeRemaining = durationInSeconds
            while (timeRemaining > 0) {
                delay(1.seconds)
                timeRemaining--
            }
            selectedHabit = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Floating Action Button
        FloatingActionButton(
            onClick = { expanded = !expanded },
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Open floating habits"
            )
        }

        // Popup with habits
        if (expanded) {
            Popup(
                alignment = Alignment.BottomEnd,
                offset = IntOffset(0, -80),
                properties = PopupProperties(focusable = true)
            ) {
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(8.dp),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Distraction Habits",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                        
                        floatingHabits.forEach { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedHabit = habit
                                        expanded = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.emoji,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.body1
                                    )
                                    if (habit.duration != null) {
                                        Text(
                                            text = habit.duration,
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            if (habit != floatingHabits.last()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Habit Modal
        if (selectedHabit != null) {
            Popup(
                alignment = Alignment.TopStart,
                offset = position,
                properties = PopupProperties(focusable = true)
            ) {
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(16.dp)
                        .onSizeChanged { size ->
                            modalSize = size
                        },
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedHabit!!.emoji,
                            style = MaterialTheme.typography.h3,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = selectedHabit!!.name,
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (selectedHabit!!.duration != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "${timeRemaining / 60}:${String.format("%02d", timeRemaining % 60)}",
                                style = MaterialTheme.typography.h4,
                                color = MaterialTheme.colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { selectedHabit = null },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
} 