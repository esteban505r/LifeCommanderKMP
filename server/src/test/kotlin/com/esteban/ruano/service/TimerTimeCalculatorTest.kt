package com.esteban.ruano.service

import com.esteban.ruano.BaseTest
import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.TimerList
import com.esteban.ruano.database.entities.TimerLists
import com.esteban.ruano.database.entities.Timers
import com.esteban.ruano.lifecommander.models.timers.TimerState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTime::class)
class TimerTimeCalculatorTest : BaseTest() {

    @Test
    fun testCalculateElapsedForRunningTimer() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 300) // 5 minutes
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.accumulatedPausedMs = 0
            
            // After 60 seconds
            val after60Seconds = startTime + 60.seconds
            val elapsed = TimerTimeCalculator.calculateElapsedMs(timer, after60Seconds)
            
            assertEquals(60000, elapsed, "Elapsed should be 60 seconds (60000 ms)")
        }
    }

    @Test
    fun testCalculateElapsedForPausedTimer() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 300)
            
            val startTime = Clock.System.now()
            val pauseTime = startTime + 60.seconds
            
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.pauseTime = pauseTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.PAUSED
            timer.accumulatedPausedMs = 0
            
            // Elapsed should be frozen at pause time
            val elapsed = TimerTimeCalculator.calculateElapsedMs(timer, pauseTime + 120.seconds)
            
            assertEquals(60000, elapsed, "Elapsed should be frozen at 60 seconds")
        }
    }

    @Test
    fun testCalculateElapsedWithMultiplePauses() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 300)
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.accumulatedPausedMs = 0
            
            // Run for 60 seconds, then pause for 30 seconds (accumulated = 30s)
            val pause1Time = startTime + 60.seconds
            timer.pauseTime = pause1Time.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.PAUSED
            timer.accumulatedPausedMs = 30000 // 30 seconds paused
            
            // Resume and run for another 60 seconds
            timer.state = TimerState.RUNNING
            timer.pauseTime = null
            val resumeTime = pause1Time + 30.seconds
            val currentTime = resumeTime + 60.seconds
            
            // Total elapsed: 60s (first run) + 60s (second run) = 120s
            // But we need to account for the pause
            // Actually, accumulatedPausedMs should be updated on resume
            // For this test, let's simulate: accumulated = 30s, running time = 120s total
            timer.accumulatedPausedMs = 30000
            
            val elapsed = TimerTimeCalculator.calculateElapsedMs(timer, currentTime)
            // Elapsed = (currentTime - startTime) - accumulatedPausedMs
            // = (150s total) - 30s paused = 120s running
            val expectedElapsed = ((currentTime - startTime).inWholeMilliseconds - 30000)
            assertEquals(expectedElapsed, elapsed, tolerance = 1000.0)
        }
    }

    @Test
    fun testCalculateRemainingForRunningTimer() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 300) // 5 minutes = 300 seconds
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.accumulatedPausedMs = 0
            
            // After 60 seconds, remaining should be 240 seconds
            val after60Seconds = startTime + 60.seconds
            val remaining = TimerTimeCalculator.calculateRemainingSeconds(timer, after60Seconds)
            
            assertEquals(240, remaining, "Remaining should be 240 seconds")
        }
    }

    @Test
    fun testCalculateRemainingForCompletedTimer() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 60) // 1 minute
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.accumulatedPausedMs = 0
            
            // After 70 seconds (past duration)
            val after70Seconds = startTime + 70.seconds
            val remaining = TimerTimeCalculator.calculateRemainingSeconds(timer, after70Seconds)
            
            assertEquals(0, remaining, "Remaining should be 0 (not negative)")
        }
    }

    @Test
    fun testShouldComplete() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 60)
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.accumulatedPausedMs = 0
            
            // Before completion
            val beforeCompletion = startTime + 30.seconds
            assertFalse(TimerTimeCalculator.shouldComplete(timer, beforeCompletion))
            
            // After completion
            val afterCompletion = startTime + 70.seconds
            assertTrue(TimerTimeCalculator.shouldComplete(timer, afterCompletion))
        }
    }

    @Test
    fun testShouldCompleteWithPauses() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 60)
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.RUNNING
            timer.accumulatedPausedMs = 30000 // 30 seconds paused
            
            // After 90 seconds total, but 30s were paused, so 60s running
            val currentTime = startTime + 90.seconds
            assertTrue(TimerTimeCalculator.shouldComplete(timer, currentTime))
        }
    }

    @Test
    fun testPausedTimerShouldNotComplete() {
        transaction {
            val user = createTestUser()
            val list = createTestTimerList(user.id.value)
            val timer = createTestTimer(list.id.value, duration = 60)
            
            val startTime = Clock.System.now()
            timer.startTime = startTime.toLocalDateTime(TimeZone.UTC)
            timer.pauseTime = (startTime + 30.seconds).toLocalDateTime(TimeZone.UTC)
            timer.state = TimerState.PAUSED
            timer.accumulatedPausedMs = 0
            
            // Even if time has passed, paused timer should not complete
            val currentTime = startTime + 120.seconds
            assertFalse(TimerTimeCalculator.shouldComplete(timer, currentTime))
        }
    }

    // Helper functions
    private fun createTestTimerList(userId: Int): TimerList {
        return transaction {
            TimerList.new {
                this.name = "Test List"
                this.userId = com.esteban.ruano.database.entities.User[userId]
                this.loopTimers = false
                this.pomodoroGrouped = false
            }
        }
    }

    private fun createTestTimer(listId: java.util.UUID, duration: Long): Timer {
        return transaction {
            Timer.new {
                this.name = "Test Timer"
                this.duration = duration
                this.enabled = true
                this.countsAsPomodoro = false
                this.list = TimerList[listId]
                this.order = 0
                this.state = TimerState.STOPPED
                this.accumulatedPausedMs = 0
                this.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
        }
    }
}

