package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifecommander.models.Tag
import com.lifecommander.models.CreateTagRequest
import com.lifecommander.models.UpdateTagRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.TokenStorageImpl
import services.tags.TagService

class TagsViewModel(
    private val tokenStorageImpl: TokenStorageImpl,
    private val tagService: TagService,
) : ViewModel() {

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags = _tags.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedTag = MutableStateFlow<Tag?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    fun loadTags() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = tagService.getAllTags(tokenStorageImpl.getToken() ?: "")
                _tags.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load tags"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun createTag(name: String, color: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val newTag = tagService.createTag(
                    tokenStorageImpl.getToken() ?: "",
                    CreateTagRequest(name, color)
                )
                loadTags() // Reload tags after creation
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create tag"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateTag(tagId: String, name: String? = null, color: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                tagService.updateTag(
                    tokenStorageImpl.getToken() ?: "",
                    tagId,
                    UpdateTagRequest(name, color)
                )
                _tags.value = _tags.value.map {
                    if (it.id == tagId) {
                        it.copy(
                            name = name ?: it.name,
                            color = color ?: it.color
                        )
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update tag"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                tagService.deleteTag(tokenStorageImpl.getToken() ?: "", tagId)
                _tags.value = _tags.value.filter { it.id != tagId }
                if (_selectedTag.value?.id == tagId) {
                    _selectedTag.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete tag"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectTag(tag: Tag?) {
        _selectedTag.value = tag
    }

    fun updateTaskTags(taskId: String, tagIds: List<String>) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                tagService.updateTaskTags(
                    tokenStorageImpl.getToken() ?: "",
                    taskId,
                    tagIds
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update task tags"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }
}

