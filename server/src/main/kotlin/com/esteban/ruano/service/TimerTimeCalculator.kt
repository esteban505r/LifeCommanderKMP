package com.esteban.ruano.service

import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.lifecommander.models.timers.TimerState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Helper class for calculating timer elapsed and remaining time
 * based on server timestamps (server-authoritative model).
 * 
 * Time calculations:
 * - If status == "running":
 *   elapsed = (nowServer - startedAt) - accumulatedPausedMs
 *   remaining = max(durationMs - elapsed, 0)
 * 
 * - If status == "paused":
 *   elapsed = (pausedAt - startedAt) - accumulatedPausedMs
 *   remaining = max(durationMs - elapsed, 0)
 */
@OptIn(ExperimentalTime::class)
object TimerTimeCalculator {
    
    /**
     * Calculate elapsed time in milliseconds for a timer
     * @param timer The timer entity
     * @param nowServer Current server time (for running timers)
     * @return Elapsed time in milliseconds
     */
    fun calculateElapsedMs(timer: Timer, nowServer: kotlin.time.Instant = Clock.System.now()): Long {
        if (timer.startTime == null) return 0L
        
        val startInstant = timer.startTime!!.toInstant(TimeZone.UTC)
        
        return when (timer.state) {
            TimerState.RUNNING -> {
                // For running timers: elapsed = (now - startedAt) - accumulatedPausedMs
                val totalElapsed = nowServer - startInstant
                (totalElapsed.inWholeMilliseconds - timer.accumulatedPausedMs).coerceAtLeast(0)
            }
            TimerState.PAUSED -> {
                // For paused timers: elapsed = (pausedAt - startedAt) - accumulatedPausedMs
                // Note: accumulatedPausedMs does NOT include the current pause duration
                // The current pause duration is (now - pauseTime), which we don't count as elapsed
                if (timer.pauseTime == null) {
                    // Edge case: paused but no pauseTime set, use accumulated only
                    timer.accumulatedPausedMs
                } else {
                    val pauseInstant = timer.pauseTime!!.toInstant(TimeZone.UTC)
                    val totalElapsed = pauseInstant - startInstant
                    // Elapsed time is the time that was actually running (total - accumulated pauses)
                    (totalElapsed.inWholeMilliseconds - timer.accumulatedPausedMs).coerceAtLeast(0)
                }
            }
            TimerState.COMPLETED, TimerState.STOPPED, TimerState.IDLE -> {
                // For stopped/completed timers, elapsed is fixed at last pause or completion
                if (timer.pauseTime != null) {
                    val pauseInstant = timer.pauseTime!!.toInstant(TimeZone.UTC)
                    val totalElapsed = pauseInstant - startInstant
                    (totalElapsed.inWholeMilliseconds - timer.accumulatedPausedMs).coerceAtLeast(0)
                } else {
                    // If no pause time, use accumulated paused time as minimum
                    timer.accumulatedPausedMs
                }
            }
        }
    }
    
    /**
     * Calculate remaining time in milliseconds for a timer
     * @param timer The timer entity
     * @param nowServer Current server time (for running timers)
     * @return Remaining time in milliseconds, never negative
     */
    fun calculateRemainingMs(timer: Timer, nowServer: kotlin.time.Instant = Clock.System.now()): Long {
        // Duration is now stored in milliseconds in the database
        val durationMs = timer.duration
        val elapsedMs = calculateElapsedMs(timer, nowServer)
        return (durationMs - elapsedMs).coerceAtLeast(0)
    }
    
    /**
     * Calculate remaining time in seconds for a timer
     * @param timer The timer entity
     * @param nowServer Current server time (for running timers)
     * @return Remaining time in seconds, never negative
     */
    fun calculateRemainingSeconds(timer: Timer, nowServer: kotlin.time.Instant = Clock.System.now()): Long {
        return calculateRemainingMs(timer, nowServer) / 1000
    }
    
    /**
     * Check if a timer should be marked as completed
     * @param timer The timer entity
     * @param nowServer Current server time
     * @return true if timer should be completed
     */
    fun shouldComplete(timer: Timer, nowServer: kotlin.time.Instant = Clock.System.now()): Boolean {
        if (timer.state != TimerState.RUNNING) return false
        if (timer.startTime == null) return false
        
        val remaining = calculateRemainingMs(timer, nowServer)
        return remaining <= 0
    }
    
    /**
     * Calculate the pause duration since last pause
     * @param timer The timer entity
     * @param nowServer Current server time
     * @return Pause duration in milliseconds, or 0 if not paused
     */
    fun calculateCurrentPauseDurationMs(timer: Timer, nowServer: kotlin.time.Instant = Clock.System.now()): Long {
        if (timer.state != TimerState.PAUSED || timer.pauseTime == null) return 0L
        
        val pauseInstant = timer.pauseTime!!.toInstant(TimeZone.UTC)
        return (nowServer - pauseInstant).inWholeMilliseconds
    }
}

