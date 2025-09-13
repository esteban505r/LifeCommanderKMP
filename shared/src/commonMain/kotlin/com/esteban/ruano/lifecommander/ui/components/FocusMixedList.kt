@file:OptIn(ExperimentalMaterialApi::class)

package ui.composables.focus

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.TaskItem
import com.esteban.ruano.ui.components.HabitItem
import com.esteban.ruano.ui.components.categorizeTasks
import com.esteban.ruano.ui.components.isDesktop
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc


@Composable
fun FocusMixedList(
    tasks: List<Task>,
    habits: List<Habit>,

    // Pull-to-refresh
    isRefreshing: Boolean,
    onPullRefresh: () -> Unit,

    // TASK actions
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskReschedule: (Task) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onHabitCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    onHabitComplete: (Habit, Boolean) -> Unit,
    onHabitEdit: (Habit) -> Unit,
    onHabitDelete: (Habit) -> Unit,
    taskItemWrapper: @Composable (content: @Composable () -> Unit, Task) -> Unit,
    habitItemWrapper: @Composable (content: @Composable () -> Unit, Habit) -> Unit,
    taskIsDone: (Task) -> Boolean,
    taskIsOverdue: (Task) -> Boolean,
    taskDueMillis: (Task) -> Long?,

    habitIsDone: (Habit) -> Boolean,
    habitIsOverdue: (Habit) -> Boolean,
    modifier: Modifier = Modifier
) {
    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onPullRefresh)

    // --- Categorize TASKS (reuse your helper to avoid dupes) ---
    val categorizedTasks = remember(tasks) { categorizeTasks(tasks) }
    val tasksOverdue  = categorizedTasks.overdue
    val tasksPending  = categorizedTasks.pending
    val tasksDone     = categorizedTasks.done

    // --- Categorize HABITS using the lambdas provided ---
    val habitsOverdue = remember(habits) { habits.filter { !habitIsDone(it) && habitIsOverdue(it) } }
    val habitsPending = remember(habits) { habits.filter { !habitIsDone(it) && !habitIsOverdue(it) } }
    val habitsDone    = remember(habits) { habits.filter { habitIsDone(it) } }

    // --- Sorting inside each section by due time (nulls last) ---
    fun <T> List<T>.sortedByDue(extract: (T) -> Long?) =
        this.sortedWith(compareBy(nullsLast()) { extract(it) })

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (!isDesktop()) Modifier.pullRefresh(pullRefreshState) else Modifier)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // OVERDUE
            focusSectionHeader(title = "Overdue".desc(), show = tasksOverdue.isNotEmpty() || habitsOverdue.isNotEmpty())
            focusMixedSection(
                tasks = tasksOverdue.sortedByDue(taskDueMillis),
                habits = habitsOverdue.sorted(),
                // task row
                onTaskClick = onTaskClick,
                onTaskCheckedChange = onTaskCheckedChange,
                onTaskEdit = onTaskEdit,
                onTaskDelete = onTaskDelete,
                onTaskReschedule = onTaskReschedule,
                taskItemWrapper = taskItemWrapper,
                taskTextDecoration = TextDecoration.None,
                // habit row
                onHabitClick = onHabitClick,
                onHabitCheckedChange = onHabitCheckedChange,
                onHabitComplete = onHabitComplete,
                onHabitEdit = onHabitEdit,
                onHabitDelete = onHabitDelete,
                habitItemWrapper = habitItemWrapper,
                habitTextDecoration = TextDecoration.None
            )

            // PENDING
            focusSectionHeader(title = "Pending".desc(), show = tasksPending.isNotEmpty() || habitsPending.isNotEmpty())
            focusMixedSection(
                tasks = tasksPending.sortedByDue(taskDueMillis),
                habits = habitsPending.sorted(),
                onTaskClick = onTaskClick,
                onTaskCheckedChange = onTaskCheckedChange,
                onTaskEdit = onTaskEdit,
                onTaskDelete = onTaskDelete,
                onTaskReschedule = onTaskReschedule,
                taskItemWrapper = taskItemWrapper,
                taskTextDecoration = TextDecoration.None,
                onHabitClick = onHabitClick,
                onHabitCheckedChange = onHabitCheckedChange,
                onHabitComplete = onHabitComplete,
                onHabitEdit = onHabitEdit,
                onHabitDelete = onHabitDelete,
                habitItemWrapper = habitItemWrapper,
                habitTextDecoration = TextDecoration.None
            )

            // DONE
            focusSectionHeader(title = "Done".desc(), show = tasksDone.isNotEmpty() || habitsDone.isNotEmpty())
            focusMixedSection(
                tasks = tasksDone.sortedByDue(taskDueMillis),
                habits = habitsDone.sorted(),
                onTaskClick = onTaskClick,
                onTaskCheckedChange = onTaskCheckedChange,
                onTaskEdit = onTaskEdit,
                onTaskDelete = onTaskDelete,
                onTaskReschedule = onTaskReschedule,
                taskItemWrapper = taskItemWrapper,
                taskTextDecoration = TextDecoration.LineThrough,
                onHabitClick = onHabitClick,
                onHabitCheckedChange = onHabitCheckedChange,
                onHabitComplete = onHabitComplete,
                onHabitEdit = onHabitEdit,
                onHabitDelete = onHabitDelete,
                habitItemWrapper = habitItemWrapper,
                habitTextDecoration = TextDecoration.LineThrough
            )

            item { Spacer(Modifier.height(64.dp)) }
        }

        if (!isDesktop()) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

private fun LazyListScope.focusSectionHeader(
    title: StringDesc,
    show: Boolean
) {
    if (!show) return
    item {
        Text(
            title.localized(),
            style = MaterialTheme.typography.h3,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .heightIn(min = 48.dp, max = 48.dp)
        )
    }
}

private fun LazyListScope.focusMixedSection(
    // TASKS
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskReschedule: (Task) -> Unit,
    taskItemWrapper: @Composable (content: @Composable () -> Unit, Task) -> Unit,
    taskTextDecoration: TextDecoration,

    // HABITS
    habits: List<Habit>,
    onHabitClick: (Habit) -> Unit,
    onHabitCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    onHabitComplete: (Habit, Boolean) -> Unit,
    onHabitEdit: (Habit) -> Unit,
    onHabitDelete: (Habit) -> Unit,
    habitItemWrapper: @Composable (content: @Composable () -> Unit, Habit) -> Unit,
    habitTextDecoration: TextDecoration,
) {
    // Render tasks first then habits (already sorted by due);
    // if you prefer a true interleave, we can merge and key on due time.
    items(tasks.size) { i ->
        val task = tasks[i]
        val interaction = remember { MutableInteractionSource() }
        TaskItem(
            task = task,
            interactionSource = interaction,
            textDecoration = taskTextDecoration,
            onCheckedChange = { task,checked -> onTaskCheckedChange(task, checked) },
            onClick = { onTaskClick(task) },
            onEdit = { onTaskEdit(task) },
            onDelete = { onTaskDelete(task) },
            onReschedule = { onTaskReschedule(task) },
            itemWrapper = { content -> taskItemWrapper(content, task) }
        )
    }
    items(habits.size) { i ->
        val habit = habits[i]
        val interaction = remember { MutableInteractionSource() }
        HabitItem(
            habit = habit,
            interactionSource = interaction,
            textDecoration = habitTextDecoration,
            onCheckedChange = onHabitCheckedChange,
            onComplete = onHabitComplete,
            onClick = { onHabitClick(habit) },
            onEdit = { onHabitEdit(habit) },
            onDelete = { onHabitDelete(habit) },
            itemWrapper = { content -> habitItemWrapper(content, habit) }
        )
    }
}
