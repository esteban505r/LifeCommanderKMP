package com.esteban.ruano.habits_presentation.ui.intent

import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.HabitReminder
import com.lifecommander.models.Habit

sealed class HabitIntent : UserIntent {
    data class FetchHabit(val id: String) : HabitIntent()
    data class FetchHabitsByDateRange(
        val filter: String? = null,
        val page: Int? = null,
        val limit: Int = 30,
        val startDate: String,
        val endDate: String,
        val isRefreshing: Boolean = false
    ) : HabitIntent()

    data class FetchHabits(
        val filter: String? = null,
        val page: Int? = null,
    ) : HabitIntent()

    data class CompleteHabit(val id: String, val onComplete : (Boolean) -> Unit) : HabitIntent()
    data class UnCompleteHabit(val id: String, val onComplete : (Boolean) -> Unit) : HabitIntent()
    data class AddHabit(
        val name: String,
        val note: String,
        val dateTime: String,
        val frequency: String,
        val reminders: List<HabitReminder>
    ) : HabitIntent()
    data class UpdateHabit(val id: String, val habit: Habit) : HabitIntent()
    data class DeleteHabit(val id: String) : HabitIntent()
    data class TrySync(val sync: () -> Unit) : HabitIntent()
    data class FetchIsOfflineModeEnabled(
        val onOnlineMode: () -> Unit,
        val onOfflineMode: () -> Unit
    ): HabitIntent()
}

sealed class HabitEffect : Effect{
    data object NavigateUp: HabitEffect()
    data class ShowSnackBar(val message:String, val type:SnackbarType):HabitEffect()
}
