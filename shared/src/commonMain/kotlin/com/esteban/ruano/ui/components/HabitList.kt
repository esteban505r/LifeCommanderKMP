package com.esteban.ruano.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.models.Habit
import com.esteban.ruano.models.HabitFrequency
import com.esteban.ruano.utils.DateUtils
import com.esteban.ruano.utils.DateUtils.compareTimes
import com.esteban.ruano.utils.DateUtils.getCurrentTime
import com.esteban.ruano.utils.DateUtils.getDelay
import com.esteban.ruano.utils.DateUtils.getTimeText
import com.esteban.ruano.utils.DateUtils.time
import dev.icerock.moko.resources.compose.stringResource
import com.esteban.ruano.shared.MR

fun LazyListScope.habitListSection(
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
            title = habitListSorted[it].name,
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
                    habitListSorted[it].getTimeText(),
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 24.sp,
                        color = if (habitListSorted[it].done == true) Color.Green else getDelay(
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

    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onPullRefresh)

    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn(Modifier.fillMaxSize()) {
            habitListSection(
                title = stringResource(MR.strings.overdue),
                habitList = habitList.filter {
                    it.done == false && compareTimes(
                        it.time() ?: "0:0",
                        getCurrentTime()
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
                title = stringResource(MR.strings.pending),
                habitList = habitList.filter {
                    it.done == false
                            && compareTimes(
                        it.time() ?: "0:0",
                        getCurrentTime()
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
                title = stringResource(MR.strings.done),
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