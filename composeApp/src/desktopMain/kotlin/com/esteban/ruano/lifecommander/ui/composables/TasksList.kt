package ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.tasks.models.TaskResponse
import ui.models.TaskFilters
import ui.screens.SectionTitle
import java.time.LocalDateTime

@Composable
fun TasksList(
    modifier: Modifier = Modifier,
    selectedFilter: TaskFilters,
    tasks: List<TaskResponse>,
    onTaskClick: (String) -> Unit,
    onEditTaskClick: (TaskResponse) -> Unit,
    onNewTaskClick: () -> Unit,
    onFilterChange: (TaskFilters) -> Unit,
    onCheckedChange: (String, Boolean) -> Unit,
    onDeleteTaskClick: (String) -> Unit,
    onRescheduleTask: (TaskResponse) -> Unit,
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item{
            SectionTitle(title = "Tasks")
        }
        item{
            LazyRow {
                TaskFilters.entries.forEach { filter ->
                    item {
                        TaskFilterChip(
                            selectedFilter = selectedFilter,
                            filter = filter,
                            onClick = { onFilterChange(filter) }
                        )
                    }
                }
            }
        }
        items(tasks.size) { index ->
            TaskCard(
                task = tasks[index],
                onCompleteChange = onCheckedChange,
                onEdit = onEditTaskClick,
                currentTime = LocalDateTime.now(),
                onDelete = onDeleteTaskClick,
                onReschedule = onRescheduleTask,
            )
        }
        item {
            NewItemCard("New Task", onClick = onNewTaskClick)
        }
    }
}