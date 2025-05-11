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
} 