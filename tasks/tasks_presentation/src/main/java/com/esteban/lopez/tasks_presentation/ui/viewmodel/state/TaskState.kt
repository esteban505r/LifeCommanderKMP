package com.esteban.ruano.tasks_presentation.ui.viewmodel.state

import com.esteban.ruano.core.utils.Constants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.tasks_domain.model.Task
import com.esteban.ruano.tasks_domain.model.TaskFilters

data class TaskState(
    val tasks: List<Task> = emptyList(),
    val calendarViewEnabled : Boolean = false,
    val dateRangeSelectedIndex : Int = 0,
    val offlineModeEnabled : Boolean = false,
    val filter : String = EMPTY_STRING,
    val isLoading : Boolean = false,
    val isRefreshing : Boolean = false,
    val isError : Boolean = false,
    val errorMessage : String = EMPTY_STRING,
    val dateRange: Pair<String?, String?>? = TaskFilters.TODAY.getDateRangeByFilter()
) : ViewState