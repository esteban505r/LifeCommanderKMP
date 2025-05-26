package com.esteban.ruano.habits_presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.button.BaseButton
import com.esteban.ruano.core_ui.theme.LightGray2
import com.esteban.ruano.core_ui.theme.LightGray3
import com.esteban.ruano.core_ui.theme.MediumGray
import com.esteban.ruano.core_ui.theme.SoftYellow
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDate
import com.lifecommander.models.Frequency
import com.esteban.ruano.habits_presentation.ui.composables.HabitReminderItem
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state.HabitDetailState
import com.esteban.ruano.habits_presentation.ui.utils.FrequencyUtils
import com.esteban.ruano.utils.HabitsUtils.date
import com.esteban.ruano.utils.HabitsUtils.time
import com.lifecommander.models.Habit
import kotlinx.coroutines.launch

@Composable
fun HabitDetailScreen(
    habitId: String,
    onNavigateUp: () -> Unit,
    onEditClick: (String) -> Unit,
    state: HabitDetailState,
    userIntent: (HabitIntent) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current


    var done by remember { mutableStateOf(state.habit?.done?:false) }
    val frequency = try {
        Frequency.valueOf((state.habit?.frequency?:"daily").uppercase())
    } catch (e: IllegalArgumentException) {
        Frequency.DAILY
    }

    Column(
        modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp).fillMaxSize()
    ) {
        AppBar(
            state.habit?.name ?: "",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            actions = {
            IconButton(onClick = {
                onEditClick(state.habit?.id!!)
            }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
        }, onClose = onNavigateUp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    FrequencyUtils.getResourceByFrequency(frequency = frequency),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    state.habit?.time() ?: "",
                    style = MaterialTheme.typography.subtitle1,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (frequency != Frequency.DAILY) {
                Text(text = getSubtitleByHabit(habit = state.habit))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    if (done) stringResource(id = R.string.done) else stringResource(id = R.string.pending),
                    style = MaterialTheme.typography.body1.copy(
                        color = if (done) MaterialTheme.colors.primary else SoftYellow, 64.sp
                    ),
                    modifier = Modifier.padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Card(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Completion rate",
                            style = MaterialTheme.typography.h4,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            "83%",
                            style = MaterialTheme.typography.h2,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Card(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Streak",
                            style = MaterialTheme.typography.h4,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            "12 days",
                            style = MaterialTheme.typography.h2,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
            Card(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                backgroundColor = Color.White,
                elevation = 0.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Best Streak",
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        "16 days",
                        style = MaterialTheme.typography.h2,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Reminders", style = MaterialTheme.typography.h3)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                    .padding(8.dp)
            ) {
                state.habit?.reminders?.forEach {
                    HabitReminderItem(
                        reminder = it,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(stringResource(id = R.string.start_time))
                Text(state.habit?.date() ?: stringResource(id = R.string.unknown))
            }
            Spacer(modifier = Modifier.height(16.dp))
            BaseButton(
                onClick = {
                    coroutineScope.launch {
                        if (done) {
                            userIntent(HabitIntent.UnCompleteHabit(state.habit?.id!!) {
                                done = it
                            })
                        } else {
                            userIntent(HabitIntent.CompleteHabit(state.habit?.id!!) {
                                done = it
                            })
                        }
                    }
                },
                text = if (done) stringResource(id = R.string.mark_as_undone) else stringResource(
                    id = R.string.mark_as_done
                ),
            )
            Row {
                Button(
                    onClick = {
                        onEditClick(state.habit?.id!!)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Edit habit", modifier = Modifier.padding(vertical = 8.dp))
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userIntent(HabitIntent.DeleteHabit(state.habit?.id!!))
                            onNavigateUp()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = LightGray3
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Delete habit", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun getSubtitleByHabit(habit: Habit?): String {
    return when(habit?.frequency){
        Frequency.DAILY.value -> stringResource(id = R.string.daily)
        Frequency.WEEKLY.value -> {
            val date = habit.date()?.toLocalDate()
            when(date?.dayOfWeek?.value){
                1 -> stringResource(id = R.string.at_day, stringResource(id = R.string.monday))
                2 -> stringResource(id = R.string.at_day, stringResource(id = R.string.tuesday))
                3 -> stringResource(id = R.string.at_day, stringResource(id = R.string.wednesday))
                4 -> stringResource(id = R.string.at_day, stringResource(id = R.string.thursday))
                5 -> stringResource(id = R.string.at_day, stringResource(id = R.string.friday))
                6 -> stringResource(id = R.string.at_day, stringResource(id = R.string.saturday))
                7 -> stringResource(id = R.string.at_day, stringResource(id = R.string.sunday))
                else -> stringResource(id = R.string.empty)
            }
        }
        Frequency.MONTHLY.value -> {
            val date = habit.date()?.toLocalDate()
            stringResource(id = R.string.at_month, date?.dayOfMonth?.toString() ?: stringResource(id = R.string.empty))
        }
        Frequency.YEARLY.value -> {
            val date = habit.date()?.toLocalDate()
            stringResource(id = R.string.at_year, date?.dayOfMonth?.toString() ?: stringResource(id = R.string.empty))
        }
        else -> stringResource(id = R.string.empty)
    }
}

@Preview
@Composable
fun HabitDetailScreenPreview() {
    Box(
        modifier = Modifier.background(color = LightGray2)
    ) {
        HabitDetailScreen(
            habitId = "1",
            onNavigateUp = {},
            onEditClick = {},
            state = HabitDetailState(
                habit = Habit(
                    id = "1",
                    name = "Drink water",
                    frequency = "daily",
                    reminders = emptyList(),
                    done = false,
                    note = "Drink water every day",
                    dateTime = "2023-10-01 00:00:00",
                )
            ),
            userIntent = {}
        )
    }
}