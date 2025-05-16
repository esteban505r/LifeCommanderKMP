package services.tasks

import com.esteban.ruano.utils.DateUIUtils.getTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Task
import kotlinx.datetime.toKotlinLocalDateTime
import utils.DateUtils.getTime
import utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime

fun Task.time() = this.dueDateTime?.toLocalDateTime()?.getTime()

fun List<Task>.sortedByDefault(): List<Task> {
    return this.sortedWith(compareBy<Task?> { it?.scheduledDateTime?.toLocalDateTime()?.compareTo(LocalDateTime.now().toKotlinLocalDateTime()) }
        .thenBy { it?.dueDateTime?.toLocalDateTime()?.compareTo(LocalDateTime.now().toKotlinLocalDateTime()) }
    )
}