package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import services.dailyjournal.DailyJournalService
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionDTO
import services.dailyjournal.models.QuestionType
import utils.StatusBarService
import services.dailyjournal.models.DailyJournalResponse

data class DailyJournalState(
    val questions: List<QuestionDTO> = emptyList(),
    val questionAnswers: List<QuestionAnswerDTO> = emptyList(),
    val journalHistory: List<DailyJournalResponse> = emptyList(),
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
                
                // Load all journal history using a wide date range
                val allHistory = dailyJournalService.getByDateRange(today, today)
                
                _state.value = _state.value.copy(
                    questions = questions,
                    questionAnswers = questionAnswers,
                    journalHistory = allHistory,
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

    fun getHistoryByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val journals = dailyJournalService.getByDateRange(
                    startDate.formatDefault(),
                    endDate.formatDefault()
                )
                _state.value = _state.value.copy(
                    journalHistory = journals,
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
                    showQuestions = questions.isNotEmpty(),
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

    fun addQuestion(question: String, type: QuestionType) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                dailyJournalService.addQuestion(question, type)
                val updatedQuestions = dailyJournalService.getQuestions()
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

    fun updateQuestion(id: String, question: String, type: QuestionType) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                dailyJournalService.updateQuestion(id, question, type)
                val updatedQuestions = dailyJournalService.getQuestions()
                _state.value = _state.value.copy(
                    questions = updatedQuestions,
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
        currentAnswers.add(QuestionAnswerDTO(questionId,"", QuestionType.TEXT,answer))
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
                    questionAnswers = _state.value.questionAnswers,
                    summary = "Daily Journal"
                )
                
                // Reload journal history after completing
                val today = getCurrentDateTime(TimeZone.currentSystemDefault()).date.formatDefault()
                val allHistory = dailyJournalService.getByDateRange("2000-01-01", today)
                
                _state.value = _state.value.copy(
                    isCompleted = true,
                    showQuestions = false,
                    journalHistory = allHistory,
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

    fun resetError() {
        _state.value = _state.value.copy(error = null)
    }
} 