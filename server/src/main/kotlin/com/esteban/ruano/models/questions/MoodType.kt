package com.esteban.ruano.models.questions

import kotlinx.serialization.Serializable

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