package com.esteban.ruano.utils

import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TimeBasedItemInfo<T>(
    val currentItem: T?,
    val nextItem: T?,
    val timeRemaining: Duration?
)

object TimeBasedUtils {
    fun <T> calculateItemTimes(
        items: List<T>,
        currentTime: LocalTime,
        getTime: (T) -> LocalTime?
    ): TimeBasedItemInfo<T> {
        val itemsWithTimes = items.mapNotNull { item ->
            getTime(item)?.let { time ->
                item to time
            }
        }

        // Find current item (closest in time, but before current hour)
        val currentItem = itemsWithTimes
            .filter { (_, time) -> 
                time <= currentTime
            }
            .maxByOrNull { (_, time) -> time }?.first

        // Find next item (closest in future)
        val nextItem = itemsWithTimes
            .filter { (_, time) -> time > currentTime }
            .minByOrNull { (_, time) -> 
                val diff = time.toSecondOfDay() - currentTime.toSecondOfDay()
                if (diff < 0) diff + 24 * 60 * 60 else diff
            }?.first

        // Calculate time remaining for current item
        val timeRemaining = nextItem?.let { item ->
            getTime(item)?.let { habitTime ->
                val diff = habitTime.toSecondOfDay() - currentTime.toSecondOfDay()
                val seconds = if (diff < 0) diff + 24 * 60 * 60 else diff
                seconds.seconds
            }
        }

        return TimeBasedItemInfo(currentItem, nextItem, timeRemaining)
    }

    /**
     * Get a time-based greeting based on the current hour
     * @param hour Current hour (0-23)
     * @return Pair of (greeting, emoji) e.g., ("Good morning", "🌅")
     */
    fun getTimeBasedGreeting(hour: Int): Pair<String, String> {
        return when (hour) {
            in 5..11 -> "Good morning" to "🌅"
            in 12..17 -> "Good afternoon" to "☀️"
            in 18..21 -> "Good evening" to "🌆"
            else -> "Good night" to "🌙"
        }
    }

    /**
     * Get a time-based message with context about pending items
     * @param hour Current hour (0-23)
     * @param pendingHabits Number of pending habits
     * @param pendingTasks Number of pending tasks
     * @return A motivational message based on time and pending items
     */
    fun getTimeBasedMessage(
        hour: Int,
        pendingHabits: Int,
        pendingTasks: Int
    ): String {
        val totalPending = pendingHabits + pendingTasks
        return when {
            hour in 5..11 -> when {
                totalPending == 0 -> "You're all set for the day! 🎉"
                totalPending <= 3 -> "A great start! You have $totalPending item${if (totalPending > 1) "s" else ""} to tackle."
                else -> "Let's make this morning productive! $totalPending items waiting for you."
            }
            hour in 12..17 -> when {
                totalPending == 0 -> "Afternoon is looking great! ✨"
                totalPending <= 3 -> "Keep the momentum going! $totalPending item${if (totalPending > 1) "s" else ""} left."
                else -> "Still time to make progress! $totalPending items to complete."
            }
            hour in 18..21 -> when {
                totalPending == 0 -> "Evening is clear! Time to relax. 🌙"
                totalPending <= 3 -> "Almost there! Just $totalPending item${if (totalPending > 1) "s" else ""} remaining."
                else -> "Let's finish strong! $totalPending items to wrap up."
            }
            else -> when {
                totalPending == 0 -> "All done! Rest well. 😴"
                else -> "You have $totalPending item${if (totalPending > 1) "s" else ""} pending. Consider planning for tomorrow."
            }
        }
    }
} 