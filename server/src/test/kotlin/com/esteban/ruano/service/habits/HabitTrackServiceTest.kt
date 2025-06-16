package com.esteban.ruano.service.habits

import com.esteban.ruano.BaseTest
import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.entities.HabitTracks
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.service.DashboardService
import com.esteban.ruano.service.HabitService
import com.esteban.ruano.testModule
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUI
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Rule
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HabitTrackServiceTest : BaseTest() {
    private val habitService: HabitService by inject()
    private val dashboardService: DashboardService by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.Companion.create {
        modules(testModule)
    }

    @BeforeTest
    override fun setup() {
        super.setup()
    }


    @Test
    fun `test create habit track`() {
        val habitId = createTestHabit()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).formatDefault().toLocalDateTimeUI()

        val success = habitService.completeHabit( habitId, doneDateTime.formatDefault(),userId)
        assertTrue(success)

        transaction {
            val track = HabitTrack.Companion.find { HabitTracks.habitId eq UUID.fromString(habitId) }.firstOrNull()
            assertNotNull(track)
            assertEquals(habitId, track.habit.id.value.toString())
            assertEquals(doneDateTime, track.doneDateTime)
            assertEquals(Status.ACTIVE, track.status)
        }
    }

    @Test
    fun `test get habits completed per day this week`() {
        // Create habits and complete them on different days
        val habit1 = createTestHabit()
        val habit2 = createTestHabit()
        val habit3 = createTestHabit()

        val today = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).date
        val monday = today.minus((today.dayOfWeek.ordinal).toLong(), DateTimeUnit.Companion.DAY)

        // Complete habits on Monday, Wednesday, and Friday
        habitService.completeHabit(habit1, monday.atTime(10, 0).formatDefault(),userId,)
        habitService.completeHabit( habit2, monday.plus(2, DateTimeUnit.Companion.DAY).atTime(10, 0).formatDefault(),userId)
        habitService.completeHabit( habit3, monday.plus(4, DateTimeUnit.Companion.DAY).atTime(10, 0).formatDefault(),userId)

        val completedPerDay = dashboardService.getDashboardData(userId, Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).formatDefault()
        ).habitsCompletedPerDayThisWeek

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
    fun `test uncomplete habit removes latest track`() {
        val habitId = createTestHabit()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())

        // Complete habit
        habitService.completeHabit( habitId, doneDateTime.formatDefault(),userId)

        // Uncomplete habit
        val success = habitService.unCompleteHabit( habitId,
            Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).formatDefault(),userId)
        assertTrue(success)

        transaction {
            val track = HabitTrack.Companion.find { HabitTracks.habitId eq UUID.fromString(habitId) }.firstOrNull()
            assertNotNull(track)
            assertEquals(Status.INACTIVE, track.status)
        }
    }

    private fun createTestHabit(): String {
        return transaction {
            val habit = Habit.Companion.new {
                baseDateTime = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())
                user = User[userId]
                name = "Test Habit ${UUID.randomUUID()}"
                note = "This is a test habit"
                status = Status.ACTIVE
            }
            habit.id.value.toString()
        }
    }
}