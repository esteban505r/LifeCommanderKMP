package com.esteban.ruano.tasks_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class TagIntent : UserIntent {
    data object LoadTags : TagIntent()
    data class CreateTag(val name: String, val color: String? = null) : TagIntent()
    data class UpdateTag(val tagId: String, val name: String?, val color: String?) : TagIntent()
    data class DeleteTag(val tagId: String) : TagIntent()
    data class UpdateTaskTags(val taskId: String, val tagIds: List<String>) : TagIntent()
}

sealed class TagEffect : Effect {
    data class ShowError(val message: String) : TagEffect()
    data object TagsUpdated : TagEffect()
}

