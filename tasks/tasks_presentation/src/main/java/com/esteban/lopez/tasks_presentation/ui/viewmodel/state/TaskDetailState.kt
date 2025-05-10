package com.esteban.ruano.tasks_presentation.ui.viewmodel.state

import com.esteban.ruano.core.domain.model.LifeCommanderException
import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.tasks_domain.model.Task

data class TaskDetailState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
):ViewState

