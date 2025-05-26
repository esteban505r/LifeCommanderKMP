package com.esteban.ruano.tasks_presentation.intent

import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task

sealed class TaskIntent : UserIntent {
    data object ToggleCalendarView : TaskIntent()
    data class FetchTask(val id: String) : TaskIntent()
    data class FetchTasks(
        val filter: String? = null,
        val page: Int? = null,
        val limit: Int = 30,
        val isRefreshing: Boolean = false
    ) : TaskIntent()

    data class CompleteTask(val id: String, val onComplete : (Boolean) -> Unit) : TaskIntent()
    data class UnCompleteTask(val id: String, val onComplete : (Boolean) -> Unit) : TaskIntent()
    data class AddTask(val name:String, val note:String?, val dueDate:String?, val scheduledDate:String?, val reminders:List<Reminder>, val onComplete: (Boolean) -> Unit) : TaskIntent()
    data class UpdateTask(val id: String, val task: Task) : TaskIntent()
    data class RescheduleTask(
        val id: String,
        val task: Task,
    ) : TaskIntent()
    data class DeleteTask(val id: String) : TaskIntent()
    data class FetchIsOfflineModeEnabled(
        val onOnlineMode: () -> Unit,
        val onOfflineMode: () -> Unit
    ): TaskIntent()
    data class TrySync(val sync: () -> Unit) : TaskIntent()
    data class ToggleOfflineMode(
        val offlineModeEnabled: Boolean,
    ) : TaskIntent()
    data object Refresh : TaskIntent()
    data class SetFilter(val filter: String) : TaskIntent()
    data class SetDateRangeSelectedIndex(val index: Int) : TaskIntent()
    data class SetDateRange(val startDate: String?, val endDate: String?) : TaskIntent()
    data object ClearDateRange : TaskIntent()
}

sealed class TaskEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : TaskEffect()
}