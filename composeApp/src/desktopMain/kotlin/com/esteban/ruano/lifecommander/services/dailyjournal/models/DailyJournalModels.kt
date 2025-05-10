package services.dailyjournal.models

data class CreateDailyJournalDTO(
    val date: String,
    val summary: String,
    val questionAnswers: List<QuestionAnswerDTO>
)

data class QuestionAnswerDTO(
    val questionId: String,
    val answer: String
)

data class QuestionDTO(
    val id: String,
    val question: String
)

data class DailyJournalResponse(
    val id: String,
    val date: String,
    val summary: String,
    val questionAnswers: List<QuestionAnswerDTO>
) 