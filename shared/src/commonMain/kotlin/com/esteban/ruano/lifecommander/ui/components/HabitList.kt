package com.esteban.ruano.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.utils.DateUIUtils.compareTimes
import com.esteban.ruano.utils.DateUIUtils.getCurrentTimeFormatted
import com.esteban.ruano.utils.HabitsUtils.time
import com.lifecommander.models.Habit
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.desc.StringDesc

fun LazyListScope.habitListSection(
    habitList: List<Habit>,
    title: StringDesc? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    onHabitClick: (Habit) -> Unit,
    onCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    onComplete: (Habit, Boolean) -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    itemWrapper: @Composable (content: @Composable () -> Unit, Habit) -> Unit
) {
    if (habitList.isEmpty()) return
    val habitListSorted = habitList.sortedBy { it.time() }
    if (title != null) {
        item {
            Text(
                title.localized(),
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .heightIn(
                        min = 48.dp,
                        max = 48.dp
                    )
            )
        }
    }
    items(habitListSorted.size) { index ->
        val habit = habitListSorted[index]
        val interactionSource = remember { MutableInteractionSource() }

        HabitItem(
            habit = habit,
            interactionSource = interactionSource,
            textDecoration = textDecoration,
            onCheckedChange = onCheckedChange,
            onComplete = onComplete,
            onClick = { onHabitClick(habit) },
            onEdit = { onEdit(habit) },
            onDelete = { onDelete(habit) },
            itemWrapper = { content -> itemWrapper(content, habit) }
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
    onCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    itemWrapper: @Composable (content: @Composable () -> Unit, Habit) -> Unit
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
            habitListSection(
                habitList = habitList.filter {
                    it.done == false && compareTimes(
                        it.time() ?: "0:0",
                        getCurrentTimeFormatted()
                    ) < 0
                },
                onHabitClick = onHabitClick,
                onCheckedChange = onCheckedChange,
                onComplete = { habit, checked ->
                    onCheckedChange(habit, checked) { }
                },
                onEdit = onEdit,
                onDelete = onDelete,
                itemWrapper = itemWrapper
            )
            habitListSection(
                habitList = habitList.filter {
                    it.done == false
                            && compareTimes(
                        it.time() ?: "0:0",
                        getCurrentTimeFormatted()
                    ) > 0
                },
                onHabitClick = onHabitClick,
                onCheckedChange = onCheckedChange,
                onComplete = { habit, checked ->
                    onCheckedChange(habit, checked) { }
                },
                onEdit = onEdit,
                onDelete = onDelete,
                itemWrapper = itemWrapper
            )
            habitListSection(
                habitList = habitList.filter { it.done == true },
                onHabitClick = onHabitClick,
                onCheckedChange = onCheckedChange,
                onComplete = { habit, checked ->
                    onCheckedChange(habit, checked) { }
                },
                textDecoration = TextDecoration.LineThrough,
                onEdit = onEdit,
                onDelete = onDelete,
                itemWrapper = itemWrapper
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