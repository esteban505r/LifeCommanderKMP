package com.esteban.ruano.habits_presentation.ui.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.CheckableItem
import com.esteban.ruano.core_ui.theme.SoftGreen
import com.esteban.ruano.core_ui.utils.DateUIUtils
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.compareTimes
import com.esteban.ruano.core_ui.utils.getColorByDelay
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_presentation.utilities.HabitsUtils.getDelay
import com.esteban.ruano.habits_presentation.utilities.HabitsUtils.getTimeText
import com.esteban.ruano.habits_presentation.utilities.HabitsUtils.time


fun LazyListScope.habitListSection(
    context: Context,
    habitList: List<Habit>,
    title: String? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    onHabitClick: (Habit) -> Unit,
    onCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    onComplete: (Habit, Boolean) -> Unit
) {
    if (habitList.isEmpty()) return
    val habitListSorted = habitList.sortedBy { it.time() }
    if (title != null) {
        item {
            Text(
                title,
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
    items(habitListSorted.size) {
        CheckableItem(
            title   = habitListSorted[it].name ?: "",
            checked = habitListSorted[it].done == true,
            textDecoration = textDecoration,
            onCheckedChange = { c ->
                onCheckedChange(habitListSorted[it], c) { checked ->
                    onComplete(habitListSorted[it], checked)
                }
            },
            onClick = {
                onHabitClick(habitListSorted[it])
            },
            suffix = {
                Text(
                    habitListSorted[it].getTimeText(context),
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 24.sp,
                        color = if (habitListSorted[it].done == true) SoftGreen else getColorByDelay(
                            habitListSorted[it].getDelay()
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HabitList(
    habitList: List<Habit>,
    isRefreshing: Boolean,
    onPullRefresh: () -> Unit,
    onHabitClick: (Habit) -> Unit,
    onCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit
) {

    var habitList by remember { mutableStateOf(habitList) }

    val context = LocalContext.current

    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onPullRefresh)

    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        val pending = stringResource(id = R.string.pending)
        val done = stringResource(id = R.string.done)
        val overdue = stringResource(id = R.string.overdue)
        LazyColumn(Modifier.fillMaxSize()) {
            habitListSection(
                context = context,
                title = overdue,
                habitList = habitList.filter {
                    it.done == false && compareTimes(
                        it.time() ?: "0:0",
                        DateUIUtils.getCurrentTime()
                    ) < 0
                },
                onHabitClick = onHabitClick,
                onCheckedChange = { task, checked, onComplete ->
                    onCheckedChange(
                        task,
                        checked,
                        onComplete
                    )
                },
                onComplete = { habit, checked ->
                    habitList = habitList.map {
                        if (it.id == habit.id) {
                            it.copy(done = checked)
                        } else {
                            it
                        }
                    }
                }
            )
            habitListSection(
                context = context,
                title = pending,
                habitList = habitList.filter {
                    it.done == false
                            && compareTimes(
                        it.time() ?: "0:0",
                        DateUIUtils.getCurrentTime()
                    ) > 0
                },
                onHabitClick = onHabitClick,
                onCheckedChange = { task, checked, onComplete ->
                    onCheckedChange(
                        task,
                        checked,
                        onComplete
                    )
                },
                onComplete = { habit, checked ->
                    habitList = habitList.map {
                        if (it.id == habit.id) {
                            it.copy(done = checked)
                        } else {
                            it
                        }
                    }
                }
            )
            habitListSection(
                context = context,
                title = done,
                habitList = habitList.filter { it.done == true },
                onHabitClick = onHabitClick,
                onCheckedChange = { task, checked, onComplete ->
                    habitList = habitList.map {
                        if (it.id == task.id) {
                            it.copy(done = checked)
                        } else {
                            it
                        }
                    }
                    onCheckedChange(task, checked, onComplete)
                },
                textDecoration = TextDecoration.LineThrough,
                onComplete = { task, checked ->
                    habitList = habitList.map {
                        if (it.id == task.id) {
                            it.copy(done = checked)
                        } else {
                            it
                        }
                    }
                }
            )
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@Preview
@Composable
fun HabitListPreview() {
    Box(
        modifier = Modifier.background(color = Color.White)
    ) {
        HabitList(
            habitList = listOf(
                Habit(
                    id = "1",
                    name = "Habit 1",
                    done = false,
                    dateTime = "01/01/2020 00:00",
                    frequency = "daily",
                ),
                Habit(
                    id = "2",
                    name = "Habit 2",
                    done = true,
                    dateTime = "01/01/2020 00:00",
                    frequency = "daily",
                ),
                Habit(
                    id = "3",
                    name = "Habit 3",
                    done = false,
                    dateTime = "01/01/2020 00:00",
                    frequency = "daily",
                ),
                Habit(
                    id = "4",
                    name = "Habit 4",
                    done = false,
                    dateTime = "01/01/2020 00:00",
                    frequency = "daily",
                ),
                Habit(
                    id = "5",
                    name = "Habit 5",
                    done = true,
                    dateTime = "01/01/2020 00:00",
                    frequency = "daily",
                ),
                Habit(
                    id = "6",
                    name = "Habit 6",
                    done = false,
                    dateTime = "01/01/2020 00:00",
                    frequency = "daily",
                ),
            ),
            isRefreshing = false,
            onPullRefresh = {},
            onHabitClick = {},
            onCheckedChange = { _, _, _ -> }
        )
    }
}