package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.dailyjournal.DailyJournalService
import services.dailyjournal.models.PomodoroResponse
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import ui.services.dailyjournal.models.CreatePomodoroRequest
import utils.DateUtils.parseDateTime
import utils.StatusBarService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DailyJournalState(
    val questions: List<QuestionDTO> = emptyList(),
    val questionAnswers: List<QuestionAnswerDTO> = emptyList(),
    val showQuestions: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val pomodoros : List<PomodoroResponse> = emptyList(),
)

class DailyJournalViewModel(
    private val dailyJournalService: DailyJournalService,
    private val statusBarService: StatusBarService
) : ViewModel() {

    private val _state = MutableStateFlow(DailyJournalState())
    val state: StateFlow<DailyJournalState> = _state.asStateFlow()

    fun loadQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val questions = dailyJournalService.getQuestions()
                _state.value = _state.value.copy(
                    questions = questions,
                    isLoading = false,
                    isCompleted = questions.isEmpty(),
                    showQuestions = questions.isNotEmpty()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false,
                    isCompleted = true,
                    showQuestions = false
                )
            }
        }
    }

    fun addQuestion(text: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                dailyJournalService.addQuestion(text)
                loadQuestions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateQuestion(id: String, text: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val updatedQuestion = dailyJournalService.updateQuestion(id, text)
                _state.value = _state.value.copy(
                    questions = _state.value.questions.map { 
                        if (it.id == id) updatedQuestion else it 
                    },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun deleteQuestion(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                dailyJournalService.deleteQuestion(id)
                _state.value = _state.value.copy(
                    questions = _state.value.questions.filter { it.id != id },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun loadPomodoros() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val result = dailyJournalService.getPomodoros(
                    startDate = LocalDate.now().format(formatter),
                    endDate = LocalDate.now().format(formatter),
                    limit = 30
                )
                _state.value = _state.value.copy(
                    pomodoros = result,
                    isLoading = false
                )
                statusBarService.updatePomodoroCount(result.size)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addSamplePomodoro(){
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val pomodoro = CreatePomodoroRequest(
                    startDateTime = LocalDateTime.now().parseDateTime(),
                    endDateTime = LocalDateTime.now().parseDateTime(),
                )
                dailyJournalService.createPomodoro(pomodoro)
                loadPomodoros()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun removeLastPomodoro() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                if (state.value.pomodoros.isNotEmpty()) {
                    val lastPomodoro = state.value.pomodoros.last()
                    dailyJournalService.removePomodoro(lastPomodoro.id)
                    loadPomodoros()
                } else {
                    _state.value = _state.value.copy(
                        error = "No pomodoros to remove",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addAnswer(questionId: String, answer: String) {
        val currentAnswers = _state.value.questionAnswers.toMutableList()
        currentAnswers.add(QuestionAnswerDTO(questionId, answer))
        _state.value = _state.value.copy(questionAnswers = currentAnswers)
    }

    fun completeDailyJournal() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                dailyJournalService.createDailyJournal(
                    date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
                    summary = "Night Block Reflection",
                    questionAnswers = _state.value.questionAnswers
                )
                _state.value = _state.value.copy(
                    isCompleted = true,
                    isLoading = false,
                    showQuestions = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false,
                    isCompleted = false,
                    showQuestions = false
                )
            }
        }
    }

    fun resetError() {
        _state.value = _state.value.copy(error = null)
    }

} 