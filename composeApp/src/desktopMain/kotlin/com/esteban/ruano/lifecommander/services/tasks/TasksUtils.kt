package services.tasks

import services.tasks.models.TaskResponse
import utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.getTime
import utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime

fun TaskResponse.time() = this.dueDateTime?.toLocalDateTime()?.getTime()

fun List<TaskResponse>.sortedByDefault(): List<TaskResponse> {
    return this.sortedWith(compareBy<TaskResponse?> { it?.scheduledDateTime?.toLocalDateTime()?.compareTo(LocalDateTime.now()) }
        .thenBy { it?.dueDateTime?.toLocalDateTime()?.compareTo(LocalDateTime.now()) }
    )
}