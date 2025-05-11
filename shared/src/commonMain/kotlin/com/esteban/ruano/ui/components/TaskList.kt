package com.esteban.ruano.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.models.Task
import com.esteban.ruano.utils.DateUtils
import com.esteban.ruano.utils.DateUtils.getCurrentTime
import com.esteban.ruano.utils.DateUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.toResourceStringBasedOnNow
import com.esteban.ruano.utils.UiUtils.getColorByPriority
import com.esteban.ruano.utils.UiUtils.getIconByPriority
import dev.icerock.moko.resources.compose.stringResource
import com.esteban.ruano.shared.MR

fun LazyListScope.taskListSection(
    taskList: List<Task>,
    title: String? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    onTaskClick: (Task) -> Unit,
    onCheckedChange: (Task, Boolean) -> Unit,
) {
    if (taskList.isEmpty()) return
    if (title != null) {
        item {
            Text(
                title,
                style = MaterialTheme.typography.h3,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .heightIn(
                        min = 48.dp,
                        max = 48.dp
                    ),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    items(taskList.size) {
        CheckableItem(
            modifier = Modifier.padding(
                bottom = if (it == taskList.size - 1) 24.dp else 8.dp
            ),
            title = taskList[it].name,
            checked = taskList[it].done == true,
            onCheckedChange = { checked ->
                onCheckedChange(taskList[it], checked)
            },
            textDecoration = textDecoration,
            onClick = {
                onTaskClick(taskList[it])
            },
            suffix = if (taskList[it].done != true) {
                {
                    val resources = taskList[it].scheduledDateTime?.toLocalDateTime()
                        ?.toResourceStringBasedOnNow()
                        ?: taskList[it].dueDateTime?.toLocalDateTime()
                            ?.toResourceStringBasedOnNow()
                    Text(
                        text = when {
                            taskList[it].scheduledDateTime != null -> "\u23F3 ${resources?.first ?: ""}"
                            taskList[it].dueDateTime != null -> "\uD83D\uDD50 ${resources?.first ?: ""}"
                            else -> resources?.first ?: ""
                        },
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 18.sp,
                            color = resources?.second ?: Color.DarkGray
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                    )
                    Icon(
                        imageVector = getIconByPriority(taskList[it].priority),
                        contentDescription = null,
                        tint = getColorByPriority(taskList[it].priority),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            } else null
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskList(
    taskList: List<Task>,
    isRefreshing: Boolean,
    onPullRefresh: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onCheckedChange: (Task, Boolean) -> Unit,
) {
    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onPullRefresh)

    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn(Modifier.fillMaxSize()) {
            taskListSection(
                title = stringResource(MR.strings.overdue),
                taskList = taskList.filter { item ->
                    (item.done == false && item.dueDateTime?.let { it.toLocalDateTime() < getCurrentTime() } ?: false)
                            ||
                            (item.done == false && item.scheduledDateTime?.let { it.toLocalDateTime() < getCurrentTime() } ?: false)
                },
                onTaskClick = onTaskClick,
                onCheckedChange = { task, checked ->
                    onCheckedChange(
                        task,
                        checked,
                    )
                },
            )
            taskListSection(
                title = stringResource(MR.strings.pending),
                taskList = taskList.filter {
                    (it.done == false && it.dueDateTime?.let { it.toLocalDateTime() >= getCurrentTime() } ?: false)
                            ||
                            (it.done == false && it.scheduledDateTime?.let { it.toLocalDateTime() >= getCurrentTime() } ?: false)
                            || it.done == false && it.dueDateTime == null && it.scheduledDateTime == null
                },
                onTaskClick = onTaskClick,
                onCheckedChange = onCheckedChange,
            )
            taskListSection(
                title = stringResource(MR.strings.done),
                taskList = taskList.filter { it.done == true },
                onTaskClick = onTaskClick,
                onCheckedChange = onCheckedChange,
                textDecoration = TextDecoration.LineThrough,
            )
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
} 