package services.tasks

import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.getTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Task
import kotlinx.datetime.TimeZone

fun Task.time() = this.dueDateTime?.toLocalDateTime()?.getTime()

fun List<Task>.sortedByDefault(): List<Task> {
    val now =  getCurrentDateTime(
        TimeZone.currentSystemDefault()
    )
    return this.sortedWith(compareBy<Task?> { it?.scheduledDateTime?.toLocalDateTime()?.compareTo(
      now ) }
        .thenBy { it?.dueDateTime?.toLocalDateTime()?.compareTo(now) }
    )
}