package com.esteban.ruano.journal_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.journal_presentation.intent.JournalEffect
import com.esteban.ruano.journal_presentation.intent.JournalIntent
import com.esteban.ruano.journal_presentation.ui.viewmodel.state.JournalState
import com.esteban.lopez.journal_domain.use_cases.JournalUseCases
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import services.dailyjournal.models.QuestionAnswerDTO
import services.dailyjournal.models.QuestionType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalUseCases: JournalUseCases
) : BaseViewModel<JournalIntent, JournalState, JournalEffect>() {

    override fun createInitialState(): JournalState = JournalState()

    override fun handleIntent(intent: JournalIntent) {
        when (intent) {
            is JournalIntent.InitializeJournal -> initializeJournal()
            is JournalIntent.LoadQuestions -> loadQuestions()
            is JournalIntent.AddAnswer -> addAnswer(intent.questionId, intent.answer)
            is JournalIntent.AddQuestion -> addQuestion(intent.question, intent.type)
            is JournalIntent.UpdateQuestion -> updateQuestion(intent.id, intent.question, intent.type)
            is JournalIntent.DeleteQuestion -> deleteQuestion(intent.id)
            is JournalIntent.CompleteDailyJournal -> completeDailyJournal()
            is JournalIntent.ResetJournal -> resetJournal()
            is JournalIntent.GetHistoryByDateRange -> getHistoryByDateRange(intent.startDate, intent.endDate)
            is JournalIntent.ResetError -> resetError()
        }
    }

    private fun initializeJournal() {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            val questionsResult = journalUseCases.getQuestions()
            val today = getCurrentDateTime(TimeZone.currentSystemDefault()).date.formatDefault()
            val journalsResult = journalUseCases.getJournalHistory(today, today)
            
            questionsResult.fold(
                onSuccess = { questions ->
                    journalsResult.fold(
                        onSuccess = { journals ->
                            val isCompleted = journals.isNotEmpty()
                            // Get unique question answers by questionId and merge with question data
                            // Maintain order by matching with questions list order
                            val questionAnswersMap = journals.flatMap { it.questionAnswers }
                                .distinctBy { it.questionId }
                                .associateBy { it.questionId }
                            
                            val questionAnswers = questions.mapNotNull { question ->
                                questionAnswersMap[question.id]?.let { answer ->
                                    answer.copy(
                                        question = question.question,
                                        type = question.type ?: QuestionType.TEXT
                                    )
                                }
                            }
                            
                            emitState {
                                currentState.copy(
                                    questions = questions,
                                    questionAnswers = questionAnswers,
                                    journalHistory = journals,
                                    isLoading = false,
                                    isCompleted = isCompleted,
                                    showQuestions = questions.isNotEmpty() && !isCompleted,
                                    error = null
                                )
                            }
                        },
                        onFailure = { e ->
                            emitState {
                                currentState.copy(
                                    error = e.message,
                                    isLoading = false,
                                    isCompleted = false,
                                    showQuestions = false
                                )
                            }
                            sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to load journal"))
                        }
                    )
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false,
                            isCompleted = false,
                            showQuestions = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to load questions"))
                }
            )
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true) }
            journalUseCases.getQuestions().fold(
                onSuccess = { questions ->
                    emitState {
                        currentState.copy(
                            questions = questions,
                            showQuestions = questions.isNotEmpty() && !currentState.isCompleted,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to load questions"))
                }
            )
        }
    }

    private fun addQuestion(question: String, type: QuestionType) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true) }
            journalUseCases.addQuestion(question, type).fold(
                onSuccess = {
                    journalUseCases.getQuestions().fold(
                        onSuccess = { updatedQuestions ->
                            emitState {
                                currentState.copy(
                                    questions = updatedQuestions,
                                    showQuestions = updatedQuestions.isNotEmpty() && !currentState.isCompleted,
                                    isLoading = false
                                )
                            }
                            sendSuccessEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Question added successfully"))
                        },
                        onFailure = { e ->
                            emitState {
                                currentState.copy(
                                    error = e.message,
                                    isLoading = false
                                )
                            }
                            sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to reload questions"))
                        }
                    )
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to add question"))
                }
            )
        }
    }

    private fun updateQuestion(id: String, question: String, type: QuestionType) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true) }
            journalUseCases.updateQuestion(id, question, type).fold(
                onSuccess = {
                    journalUseCases.getQuestions().fold(
                        onSuccess = { updatedQuestions ->
                            emitState {
                                currentState.copy(
                                    questions = updatedQuestions,
                                    isLoading = false
                                )
                            }
                            sendSuccessEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Question updated successfully"))
                        },
                        onFailure = { e ->
                            emitState {
                                currentState.copy(
                                    error = e.message,
                                    isLoading = false
                                )
                            }
                            sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to reload questions"))
                        }
                    )
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to update question"))
                }
            )
        }
    }

    private fun deleteQuestion(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true) }
            journalUseCases.deleteQuestion(id).fold(
                onSuccess = {
                    val updatedQuestions = currentState.questions.filter { it.id != id }
                    emitState {
                        currentState.copy(
                            questions = updatedQuestions,
                            showQuestions = updatedQuestions.isNotEmpty() && !currentState.isCompleted,
                            isLoading = false
                        )
                    }
                    sendSuccessEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Question deleted successfully"))
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to delete question"))
                }
            )
        }
    }

    private fun addAnswer(questionId: String, answer: String) {
        val currentAnswers = currentState.questionAnswers.toMutableList()
        val existingAnswerIndex = currentAnswers.indexOfFirst { it.questionId == questionId }
        
        if (existingAnswerIndex >= 0) {
            // Update existing answer while preserving question data
            val existingAnswer = currentAnswers[existingAnswerIndex]
            currentAnswers[existingAnswerIndex] = existingAnswer.copy(answer = answer)
        } else {
            // Create new answer, but try to get question data from questions list
            val question = currentState.questions.firstOrNull { it.id == questionId }
            currentAnswers.add(
                QuestionAnswerDTO(
                    questionId = questionId,
                    question = question?.question ?: "",
                    type = question?.type ?: QuestionType.TEXT,
                    answer = answer
                )
            )
        }
        
        emitState {
            currentState.copy(questionAnswers = currentAnswers)
        }
    }

    private fun completeDailyJournal() {
        viewModelScope.launch {
            // Prevent duplicate calls
            if (currentState.isSaving) {
                return@launch
            }
            
            emitState { currentState.copy(isLoading = true, isSaving = true) }
            val today = getCurrentDateTime(TimeZone.currentSystemDefault())
            val todayDate = today.date.formatDefault()
            
            // Check if journal already exists for today
            journalUseCases.getJournalByDate(todayDate).fold(
                onSuccess = { existingJournal ->
                    if (existingJournal != null) {
                        // Update existing journal using PATCH
                        journalUseCases.updateDailyJournal(
                            id = existingJournal.id,
                            summary = "Daily Journal",
                            questionAnswers = currentState.questionAnswers
                        ).fold(
                            onSuccess = {
                                journalUseCases.getJournalHistory(todayDate, todayDate).fold(
                                    onSuccess = { allHistory ->
                                        // Extract question answers from the journal response and ensure they have question text and type
                                        // Maintain order by matching with questions list order
                                        val journalEntry = allHistory.firstOrNull()
                                        val questionAnswersMap = journalEntry?.questionAnswers
                                            ?.distinctBy { it.questionId }
                                            ?.associateBy { it.questionId } ?: emptyMap()
                                        
                                        val updatedQuestionAnswers = currentState.questions.mapNotNull { question ->
                                            questionAnswersMap[question.id]?.let { answer ->
                                                answer.copy(
                                                    question = question.question,
                                                    type = question.type ?: QuestionType.TEXT
                                                )
                                            }
                                        }
                                        
                                        emitState {
                                            currentState.copy(
                                                isCompleted = true,
                                                showQuestions = false,
                                                journalHistory = allHistory,
                                                questionAnswers = updatedQuestionAnswers,
                                                isLoading = false,
                                                isSaving = false
                                            )
                                        }
                                        sendSuccessEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Journal updated successfully"))
                                    },
                                    onFailure = { e ->
                                        emitState {
                                            currentState.copy(
                                                error = e.message,
                                                isLoading = false,
                                                isSaving = false
                                            )
                                        }
                                        sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to reload history"))
                                    }
                                )
                            },
                            onFailure = { e ->
                                emitState {
                                    currentState.copy(
                                        error = e.message,
                                        isLoading = false,
                                        isSaving = false
                                    )
                                }
                                sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to update journal"))
                            }
                        )
                    } else {
                        // Create new journal using POST
                        journalUseCases.createDailyJournal(
                            date = todayDate,
                            summary = "Daily Journal",
                            questionAnswers = currentState.questionAnswers
                        ).fold(
                            onSuccess = {
                                journalUseCases.getJournalHistory(todayDate, todayDate).fold(
                                    onSuccess = { allHistory ->
                                        // Extract question answers from the journal response and ensure they have question text and type
                                        // Maintain order by matching with questions list order
                                        val journalEntry = allHistory.firstOrNull()
                                        val questionAnswersMap = journalEntry?.questionAnswers
                                            ?.distinctBy { it.questionId }
                                            ?.associateBy { it.questionId } ?: emptyMap()
                                        
                                        val updatedQuestionAnswers = currentState.questions.mapNotNull { question ->
                                            questionAnswersMap[question.id]?.let { answer ->
                                                answer.copy(
                                                    question = question.question,
                                                    type = question.type ?: QuestionType.TEXT
                                                )
                                            }
                                        }
                                        
                                        emitState {
                                            currentState.copy(
                                                isCompleted = true,
                                                showQuestions = false,
                                                journalHistory = allHistory,
                                                questionAnswers = updatedQuestionAnswers,
                                                isLoading = false,
                                                isSaving = false
                                            )
                                        }
                                        sendSuccessEffect(com.esteban.ruano.core.utils.UiText.DynamicString("Journal completed successfully"))
                                    },
                                    onFailure = { e ->
                                        emitState {
                                            currentState.copy(
                                                error = e.message,
                                                isLoading = false,
                                                isSaving = false
                                            )
                                        }
                                        sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to reload history"))
                                    }
                                )
                            },
                            onFailure = { e ->
                                emitState {
                                    currentState.copy(
                                        error = e.message,
                                        isLoading = false,
                                        isSaving = false
                                    )
                                }
                                sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to complete journal"))
                            }
                        )
                    }
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false,
                            isSaving = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to check existing journal"))
                }
            )
        }
    }

    private fun getHistoryByDateRange(startDate: kotlinx.datetime.LocalDate, endDate: kotlinx.datetime.LocalDate) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true) }
            journalUseCases.getJournalHistory(
                startDate.formatDefault(),
                endDate.formatDefault()
            ).fold(
                onSuccess = { journals ->
                    emitState {
                        currentState.copy(
                            journalHistory = journals,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to load history"))
                }
            )
        }
    }

    private fun resetJournal() {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true) }
            val questionsResult = journalUseCases.getQuestions()
            
            questionsResult.fold(
                onSuccess = { questions ->
                    // Preserve existing answers when resetting
                    val preservedAnswers = currentState.questionAnswers
                    emitState {
                        currentState.copy(
                            questions = questions,
                            questionAnswers = preservedAnswers,
                            isCompleted = false,
                            showQuestions = questions.isNotEmpty(),
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            error = e.message,
                            isLoading = false
                        )
                    }
                    sendErrorEffect(com.esteban.ruano.core.utils.UiText.DynamicString(e.message ?: "Failed to reset journal"))
                }
            )
        }
    }

    private fun resetError() {
        emitState { currentState.copy(error = null) }
    }
}

