package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.models.StudyItem
import com.lifecommander.models.StudySession
import com.lifecommander.models.StudyTopic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import services.auth.TokenStorageImpl
import services.study.StudyService

data class StudyState(
    val topics: List<StudyTopic> = emptyList(),
    val items: List<StudyItem> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val disciplines: List<String> = emptyList(),
    val topicsLoading: Boolean = false,
    val itemsLoading: Boolean = false,
    val sessionsLoading: Boolean = false,
    val error: String? = null
)

class StudyViewModel(
    private val studyService: StudyService,
    private val tokenStorageImpl: TokenStorageImpl
) : ViewModel() {

    private val _state = MutableStateFlow(StudyState())
    val state: StateFlow<StudyState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        loadTopics()
        loadItems()
        loadSessions()
        loadDisciplines()
    }

    fun loadDisciplines() {
        viewModelScope.launch {
            try {
                val disciplines = studyService.getDisciplines()
                _state.value = _state.value.copy(disciplines = disciplines)
            } catch (e: Exception) {
                // Silently fail - disciplines are optional
            }
        }
    }

    fun loadTopics() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(topicsLoading = true, error = null)
                val topics = studyService.getTopics()
                _state.value = _state.value.copy(
                    topics = topics,
                    topicsLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    topicsLoading = false
                )
            }
        }
    }

    fun loadItems() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(itemsLoading = true, error = null)
                val items = studyService.getItems()
                _state.value = _state.value.copy(
                    items = items,
                    itemsLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    itemsLoading = false
                )
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(sessionsLoading = true, error = null)
                val sessions = studyService.getSessions()
                _state.value = _state.value.copy(
                    sessions = sessions,
                    sessionsLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    sessionsLoading = false
                )
            }
        }
    }

    fun createTopic(topic: StudyTopic) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.createTopic(topic)
                loadTopics()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateTopic(id: String, topic: StudyTopic) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.updateTopic(id, topic)
                loadTopics()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteTopic(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.deleteTopic(id)
                loadTopics()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun createItem(item: StudyItem) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.createItem(item)
                loadItems()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateItem(id: String, item: StudyItem) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.updateItem(id, item)
                loadItems()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.deleteItem(id)
                loadItems()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun createSession(session: StudySession) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                val now = getCurrentDateTime(TimeZone.currentSystemDefault())
                val sessionWithStart = session.copy(
                    actualStart = session.actualStart ?: now.formatDefault()
                )
                studyService.createSession(sessionWithStart)
                loadSessions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateSession(id: String, session: StudySession) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.updateSession(id, session)
                loadSessions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun completeSession(id: String, actualEnd: String, notes: String? = null) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.completeSession(id, actualEnd, notes)
                loadSessions()
                loadItems() // Reload items in case progress was updated
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null)
                studyService.deleteSession(id)
                loadSessions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

