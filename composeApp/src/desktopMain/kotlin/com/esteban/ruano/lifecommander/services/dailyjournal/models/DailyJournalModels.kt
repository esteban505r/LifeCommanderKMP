package services.dailyjournal.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateDailyJournalDTO(
    val date: String,
    val summary: String,
    val questionAnswers: List<QuestionAnswerDTO>
)

@Serializable
data class QuestionAnswerDTO(
    val questionId: String,
    val question: String,
    val type: QuestionType? = QuestionType.TEXT,
    val answer: String,
    val mood: MoodType? = null
)

@Serializable
data class QuestionDTO(
    val id: String,
    val question: String,
    val type: QuestionType? = QuestionType.TEXT
)

@Serializable
data class DailyJournalResponse(
    val id: String,
    val date: String,
    val summary: String,
    val questionAnswers: List<QuestionAnswerDTO>
)

@Serializable
enum class QuestionType {
    TEXT,
    MOOD
}

@Serializable
enum class MoodType(val emoji: String, val label: String, val category: String) {
    // Positive Emotions
    JOY("😊", "Joy", "Positive"),
    HAPPINESS("😄", "Happiness", "Positive"),
    EXCITEMENT("🤩", "Excitement", "Positive"),
    GRATITUDE("🙏", "Gratitude", "Positive"),
    LOVE("🥰", "Love", "Positive"),
    CONTENTMENT("😌", "Contentment", "Positive"),
    CONFIDENCE("😎", "Confidence", "Positive"),
    INSPIRATION("✨", "Inspiration", "Positive"),
    PRIDE("😤", "Pride", "Positive"),
    HOPE("🤗", "Hope", "Positive"),
    
    // Neutral Emotions
    NEUTRAL("😐", "Neutral", "Neutral"),
    CALM("😌", "Calm", "Neutral"),
    FOCUSED("🧘", "Focused", "Neutral"),
    BALANCED("⚖️", "Balanced", "Neutral"),
    REFLECTIVE("🤔", "Reflective", "Neutral"),
    
    // Negative Emotions
    SADNESS("😔", "Sadness", "Negative"),
    ANGER("😠", "Anger", "Negative"),
    ANXIETY("😰", "Anxiety", "Negative"),
    FEAR("😨", "Fear", "Negative"),
    FRUSTRATION("😤", "Frustration", "Negative"),
    DISAPPOINTMENT("😞", "Disappointment", "Negative"),
    LONELINESS("🥺", "Loneliness", "Negative"),
    GUILT("😣", "Guilt", "Negative"),
    SHAME("😳", "Shame", "Negative"),
    JEALOUSY("😒", "Jealousy", "Negative"),
    
    // Physical States
    TIRED("😴", "Tired", "Physical"),
    STRESSED("😫", "Stressed", "Physical"),
    OVERWHELMED("😵", "Overwhelmed", "Physical"),
    RESTLESS("😬", "Restless", "Physical"),
    RELAXED("😌", "Relaxed", "Physical"),
    
    // Complex Emotions
    NOSTALGIA("🥲", "Nostalgia", "Complex"),
    CONFUSION("😕", "Confusion", "Complex"),
    SURPRISE("😲", "Surprise", "Complex"),
    CURIOSITY("🤨", "Curiosity", "Complex"),
    AMBIVALENCE("😶", "Ambivalence", "Complex")
} 