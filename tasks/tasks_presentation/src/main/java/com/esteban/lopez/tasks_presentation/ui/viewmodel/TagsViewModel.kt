package com.esteban.ruano.tasks_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.tasks_presentation.intent.TagEffect
import com.esteban.ruano.tasks_presentation.intent.TagIntent
import com.esteban.ruano.tasks_presentation.ui.viewmodel.state.TagsState
import com.lifecommander.models.CreateTagRequest
import com.lifecommander.models.Tag
import com.lifecommander.models.UpdateTagRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import services.tags.TagService
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagService: TagService,
    private val preferences: Preferences
) : BaseViewModel<TagIntent, TagsState, TagEffect>() {

    private fun loadTags() {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true, error = null)
            }
            try {
                val token = preferences.loadAuthToken().first()
                if (token.isEmpty()) {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            error = "Not authenticated"
                        )
                    }
                    return@launch
                }
                val tags = tagService.getAllTags(token)
                emitState {
                    currentState.copy(tags = tags, isLoading = false)
                }
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load tags"
                    )
                }
            }
        }
    }

    private fun createTag(name: String, color: String?) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true, error = null)
            }
            try {
                val token = preferences.loadAuthToken().first()
                if (token.isEmpty()) {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            error = "Not authenticated"
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Not authenticated"))
                    return@launch
                }
                tagService.createTag(token, CreateTagRequest(name, color))
                loadTags() // Reload tags after creation
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to create tag"
                emitState {
                    currentState.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(errorMessage))
            }
        }
    }

    private fun updateTag(tagId: String, name: String?, color: String?) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true, error = null)
            }
            try {
                val token = preferences.loadAuthToken().first()
                if (token.isEmpty()) {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            error = "Not authenticated"
                        )
                    }
                    return@launch
                }
                val success = tagService.updateTag(token, tagId, UpdateTagRequest(name, color))
                if (!success) {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            error = "Failed to update tag"
                        )
                    }
                    return@launch
                }
                loadTags() // Reload tags after update
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update tag"
                    )
                }
            }
        }
    }

    private fun deleteTag(tagId: String) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true, error = null)
            }
            try {
                val token = preferences.loadAuthToken().first()
                if (token.isEmpty()) {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            error = "Not authenticated"
                        )
                    }
                    return@launch
                }
                tagService.deleteTag(token, tagId)
                loadTags() // Reload tags after deletion
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete tag"
                    )
                }
            }
        }
    }

    private fun updateTaskTags(taskId: String, tagIds: List<String>) {
        viewModelScope.launch {
            emitState {
                currentState.copy(isLoading = true, error = null)
            }
            try {
                val token = preferences.loadAuthToken().first()
                if (token.isEmpty()) {
                    emitState {
                        currentState.copy(
                            isLoading = false,
                            error = "Not authenticated"
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Not authenticated"))
                    return@launch
                }
                tagService.updateTaskTags(token, taskId, tagIds)
                emitState {
                    currentState.copy(isLoading = false)
                }
                // Emit effect to trigger task list refresh
                sendEffect({ TagEffect.TagsUpdated })
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update task tags"
                    )
                }
                sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to update task tags"))
            }
        }
    }

    override fun createInitialState(): TagsState {
        return TagsState()
    }

    override fun handleIntent(intent: TagIntent) {
        when (intent) {
            is TagIntent.LoadTags -> loadTags()
            is TagIntent.CreateTag -> createTag(intent.name, intent.color)
            is TagIntent.UpdateTag -> updateTag(intent.tagId, intent.name, intent.color)
            is TagIntent.DeleteTag -> deleteTag(intent.tagId)
            is TagIntent.UpdateTaskTags -> updateTaskTags(intent.taskId, intent.tagIds)
        }
    }
}

