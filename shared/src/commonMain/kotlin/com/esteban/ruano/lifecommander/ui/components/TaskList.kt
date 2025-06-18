package com.esteban.ruano.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Task
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.datetime.TimeZone

@Composable
expect fun isDesktop(): Boolean

fun LazyListScope.taskListSection(
    taskList: List<Task>,
    title: StringDesc? = null,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onReschedule: (Task) -> Unit,
    textDecoration: TextDecoration = TextDecoration.None,
    onTaskClick: (Task) -> Unit,
    onCheckedChange: (Task, Boolean) -> Unit,
    itemWrapper: @Composable (content: @Composable () -> Unit, Task) -> Unit
) {
    if (taskList.isEmpty()) return
    if (title != null) {
        item {
            Text(
                title.localized(),
                style = MaterialTheme.typography.h3,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .heightIn(
                        min = 48.dp,
                        max = 48.dp
                    ),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    items(taskList.size) { index ->
        val task = taskList[index]
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        TaskItem(
            task = task,
            interactionSource = interactionSource,
            isHovered = isHovered,
            onCheckedChange = { task, checked ->
                onCheckedChange(task, checked)
            },
            onEdit = { onEdit.invoke(task) },
            onClick = {
                onTaskClick(task)
            },
            textDecoration = textDecoration,
            onDelete = { onDelete.invoke(task) },
            onReschedule = { onReschedule.invoke(task) },
            itemWrapper = { content -> itemWrapper(content, task) }
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
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onReschedule: (Task) -> Unit,
    modifier: Modifier = Modifier,
    itemWrapper: @Composable (content: @Composable () -> Unit, Task) -> Unit,
) {
    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onPullRefresh)

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (!isDesktop()) {
                    Modifier.pullRefresh(pullRefreshState)
                } else Modifier
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Overdue tasks
            taskListSection(
                taskList = taskList.filter { item ->
                    (item.done == false && item.dueDateTime?.let {
                        it.toLocalDateTime() < getCurrentDateTime(
                            TimeZone.currentSystemDefault()
                        )
                    } ?: false)
                            ||
                            (item.done == false && item.scheduledDateTime?.let {
                                it.toLocalDateTime() < getCurrentDateTime(
                                    TimeZone.currentSystemDefault()
                                )
                            } ?: false)
                },
                onTaskClick = onTaskClick,
                onCheckedChange = { task, checked ->
                    onCheckedChange(task, checked)
                },
                onEdit = onEdit,
                onDelete = onDelete,
                onReschedule = onReschedule,
                itemWrapper = itemWrapper,
            )
            // Pending tasks
            taskListSection(
                taskList = taskList.filter {
                    (it.done == false && it.dueDateTime?.let {
                        it.toLocalDateTime() >= getCurrentDateTime(
                            TimeZone.currentSystemDefault()
                        )
                    } ?: false)
                            ||
                            (it.done == false && it.scheduledDateTime?.let {
                                it.toLocalDateTime() >= getCurrentDateTime(
                                    TimeZone.currentSystemDefault()
                                )
                            } ?: false)
                            || it.done == false && it.dueDateTime == null && it.scheduledDateTime == null
                },
                onTaskClick = onTaskClick,
                onCheckedChange = onCheckedChange,
                onEdit = onEdit,
                onDelete = onDelete,
                onReschedule = onReschedule,
                itemWrapper = itemWrapper,
            )
            // Done tasks
            taskListSection(
                taskList = taskList.filter { it.done == true },
                onTaskClick = onTaskClick,
                onCheckedChange = onCheckedChange,
                textDecoration = TextDecoration.LineThrough,
                onEdit = onEdit,
                onDelete = onDelete,
                onReschedule = onReschedule,
                itemWrapper = itemWrapper,
            )
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
        if (!isDesktop()) {
            PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
} 