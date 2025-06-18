package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
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
)

class DailyJournalViewModel(
    private val dailyJournalService: DailyJournalService,
    private val statusBarService: StatusBarService
) : ViewModel() {

    private val _state = MutableStateFlow(DailyJournalState())
    val state: StateFlow<DailyJournalState> = _state.asStateFlow()

    fun initializeJournal() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Load questions first
                val questions = dailyJournalService.getQuestions()
                
                // Check if completed for today
                val today = getCurrentDateTime(
                    TimeZone.currentSystemDefault()
                ).date.formatDefault()
                val journals = dailyJournalService.getByDateRange(today, today)
                val isCompleted = journals.isNotEmpty()
                val questionAnswers = journals.flatMap { it.questionAnswers }
                
                _state.value = _state.value.copy(
                    questions = questions,
                    questionAnswers = questionAnswers,
                    isLoading = false,
                    isCompleted = isCompleted,
                    showQuestions = questions.isNotEmpty() && !isCompleted,
                    error = null
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

    fun checkIfCompleted() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val today = getCurrentDateTime(
                    TimeZone.currentSystemDefault()
                ).date.formatDefault()
                val journals = dailyJournalService.getByDateRange(today, today)
                _state.value = _state.value.copy(
                    isCompleted = journals.isNotEmpty(),
                    questionAnswers = journals.flatMap { it.questionAnswers },
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
                initializeJournal()
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
                val updatedQuestions = _state.value.questions.filter { it.id != id }
                _state.value = _state.value.copy(
                    questions = updatedQuestions,
                    showQuestions = updatedQuestions.isNotEmpty() && !_state.value.isCompleted,
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

    fun addAnswer(questionId: String, answer: String) {
        val currentAnswers = _state.value.questionAnswers.filterNot {
            it.questionId == questionId
        }.toMutableList()
        currentAnswers.add(QuestionAnswerDTO(questionId, answer))
        _state.value = _state.value.copy(questionAnswers = currentAnswers)
    }

    fun completeDailyJournal() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                dailyJournalService.createDailyJournal(
                    date = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault(),
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