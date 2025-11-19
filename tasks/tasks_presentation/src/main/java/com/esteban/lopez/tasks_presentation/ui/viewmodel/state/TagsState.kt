package com.esteban.ruano.tasks_presentation.ui.viewmodel.state

import com.esteban.lopez.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core_ui.view_model.ViewState
import com.lifecommander.models.Tag

data class TagsState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState

