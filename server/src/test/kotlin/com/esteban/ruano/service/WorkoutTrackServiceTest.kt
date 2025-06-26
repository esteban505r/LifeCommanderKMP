package com.esteban.ruano.service

import com.esteban.ruano.BaseTest
import com.esteban.ruano.database.entities.Exercise
import com.esteban.ruano.database.entities.Exercises
import com.esteban.ruano.database.entities.WorkoutDay
import com.esteban.ruano.database.entities.WorkoutDays
import com.esteban.ruano.database.entities.WorkoutTrack
import com.esteban.ruano.database.entities.WorkoutTracks
import com.esteban.ruano.database.models.MuscleGroup
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.testModule
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUI
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.koin.test.mock.MockProviderRule
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorkoutTrackServiceTest : BaseTest() {
    private val workoutService: WorkoutService by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(testModule)
    }

    @BeforeTest
    override fun setup() {
        super.setup()
    }

    @Test
    fun `test create workout track`() {
        val workoutDayId = createTestWorkoutDay()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formatDefault().toLocalDateTimeUI()
        
        val success = workoutService.completeWorkout(userId, workoutDayId.day, doneDateTime.formatDefault())
        assertTrue(success)
        
        transaction {
            val track = WorkoutTrack.find { WorkoutTracks.doneDateTime eq doneDateTime }.firstOrNull()
            assertNotNull(track)
            assertEquals(doneDateTime, track.doneDateTime)
            assertEquals(Status.ACTIVE, track.status)
        }
    }

    @Test
    fun `test get workouts completed per day this week`() {
        // Create workout days and complete them on different days
        val workoutDay1 = createTestWorkoutDay()
        val workoutDay2 = createTestWorkoutDay()
        val workoutDay3 = createTestWorkoutDay()
        
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monday = today.minus((today.dayOfWeek.ordinal).toLong(), DateTimeUnit.DAY)
        
        // Complete workouts on Monday, Wednesday, and Friday
        workoutService.completeWorkout(userId, workoutDay1.day, monday.atTime(10, 0).formatDefault())
        workoutService.completeWorkout(userId, workoutDay2.day, monday.plus(2, DateTimeUnit.DAY).atTime(10, 0).formatDefault())
        workoutService.completeWorkout(userId, workoutDay3.day, monday.plus(4, DateTimeUnit.DAY).atTime(10, 0).formatDefault())
        
        val completedPerDay = workoutService.getWorkoutsCompletedPerDayThisWeek(userId)
        
        assertEquals(7, completedPerDay.size)
        assertEquals(1, completedPerDay[0]) // Monday
        assertEquals(0, completedPerDay[1]) // Tuesday
        assertEquals(1, completedPerDay[2]) // Wednesday
        assertEquals(0, completedPerDay[3]) // Thursday
        assertEquals(1, completedPerDay[4]) // Friday
        assertEquals(0, completedPerDay[5]) // Saturday
        assertEquals(0, completedPerDay[6]) // Sunday
    }

    @Test
    fun `test uncomplete workout removes latest track`() {
        val workoutDayId = createTestWorkoutDay()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // Complete workout
        workoutService.completeWorkout(userId, workoutDayId.day, doneDateTime.formatDefault())
        
        // Get the created track ID
        val trackId = transaction {
            WorkoutTrack.find { WorkoutTracks.doneDateTime eq doneDateTime.formatDefault().toLocalDateTimeUI() }.firstOrNull()?.id?.value
        }
        assertNotNull(trackId)
        
        // Uncomplete workout
        val success = workoutService.unCompleteWorkout(userId, trackId.toString())
        assertTrue(success)
        
        transaction {
            val track = WorkoutTrack.find { WorkoutTracks.id eq trackId }.firstOrNull()
            assertNotNull(track)
            assertEquals(Status.INACTIVE, track.status)
        }
    }

    @Test
    fun `test get workout tracks by date range`() {
        val workoutDayId = createTestWorkoutDay()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // Complete workout today
        workoutService.completeWorkout(userId, workoutDayId.day, today.formatDefault())
        
        val weekStart = today.date.minus((today.date.dayOfWeek.ordinal).toLong(), DateTimeUnit.DAY)
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
        
        val tracks = workoutService.getWorkoutTracksByDateRange(
            userId, 
            weekStart.formatDefault(), 
            weekEnd.formatDefault()
        )
        
        assertEquals(1, tracks.size)
        assertEquals(today.formatDefault(), tracks[0].doneDateTime)
    }

    @Test
    fun `test delete workout track`() {
        val workoutDayId = createTestWorkoutDay()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // Complete workout
        workoutService.completeWorkout(userId, workoutDayId.day, doneDateTime.formatDefault())
        
        // Get the created track ID
        val trackId = transaction {
            WorkoutTrack.find { WorkoutTracks.doneDateTime eq doneDateTime.formatDefault().toLocalDateTimeUI() }.firstOrNull()?.id?.value
        }
        assertNotNull(trackId)
        
        // Delete track
        val success = workoutService.deleteWorkoutTrack(userId, trackId.toString())
        assertTrue(success)
        
        transaction {
            val track = WorkoutTrack.find { WorkoutTracks.id eq trackId }.firstOrNull()
            assertNotNull(track)
            assertEquals(Status.DELETED, track.status)
        }
    }

    private fun createTestWorkoutDay(): WorkoutDay {
        return transaction {
            val workoutDay = WorkoutDay.new {
                user = com.esteban.ruano.database.entities.User[userId]
                name = "Test Workout Day ${UUID.randomUUID()}"
                day = 1 // Monday
                time = LocalTime(10, 0)
                status = Status.ACTIVE
            }
            
            // Create a test exercise and associate it with the workout day
            val exercise = Exercise.new {
                user = com.esteban.ruano.database.entities.User[userId]
                name = "Test Exercise ${UUID.randomUUID()}"
                description = "This is a test exercise"
                restSecs = 60
                baseSets = 3
                baseReps = 10
                muscleGroup = MuscleGroup.CORE
                status = Status.ACTIVE
            }
            
            // Associate exercise with workout day
            com.esteban.ruano.database.entities.ExerciseWithWorkoutDay.new {
                this.exercise = exercise
                this.workoutDay = workoutDay
                this.user = com.esteban.ruano.database.entities.User[userId]
            }
            
            workoutDay
        }
    }
} 