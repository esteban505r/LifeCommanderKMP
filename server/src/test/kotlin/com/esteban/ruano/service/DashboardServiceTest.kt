package com.esteban.ruano.service

import com.esteban.ruano.BaseTest
import com.esteban.ruano.TestDatabaseConfig
import com.esteban.ruano.database.converters.toHabitDTO
import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.Habits
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.entities.HabitTracks
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.testModule
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUtils
import com.esteban.ruano.utils.HabitUtils
import com.lifecommander.models.Frequency
import kotlinx.datetime.*
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals

class DashboardServiceTest : BaseTest() {
    private val dashboardService: DashboardService by inject()
    private val habitService: HabitService by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(testModule)
    }

    @Test
    fun `test daily habit overdue logic`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // Monday 9:00 AM
        
        // Create test habits in database
        val habit1Id = createTestHabit("Daily Exercise", "DAILY", baseDateTime)
        val futureHabitId = createTestHabit("Future Habit", "DAILY", LocalDateTime(2024, 1, 20, 9, 0))
        
        // Test 1: Habit should be done today but time has passed
        val currentTime1 = LocalDateTime(2024, 1, 15, 14, 0) // Monday 2:00 PM
        val overdueHabits1 = dashboardService.getOverdueHabitsList(getHabitsFromDB(currentTime1.date), currentTime1)
        assertTrue(overdueHabits1.isNotEmpty(), "Daily habit should be overdue when time has passed")
        assertEquals(1, overdueHabits1.size)
        assertTrue(overdueHabits1.any { it.name == "Daily Exercise" })
        
        // Test 2: Habit should be done today but time hasn't passed yet
        val currentTime2 = LocalDateTime(2024, 1, 15, 8, 0) // Monday 8:00 AM
        val overdueHabits2 = dashboardService.getOverdueHabitsList(getHabitsFromDB(currentTime2.date), currentTime2)
        assertTrue(overdueHabits2.isEmpty(), "Daily habit should not be overdue when time hasn't passed")
        
        // Test 3: Habit base date is in the future
        val overdueHabits3 = dashboardService.getOverdueHabitsList(getHabitsFromDB(currentTime1.date), currentTime1)
        assertFalse(overdueHabits3.any { it.name == "Future Habit" }, "Future daily habit should not be overdue")
    }

    @Test
    fun `test weekly habit overdue logic`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // Monday 9:00 AM
        
        // Create test habit in database
        createTestHabit("Weekly Meeting", "WEEKLY", baseDateTime)
        
        // Test 1: Today is Monday (target day) and time has passed
        val mondayAfternoon = LocalDateTime(2024, 1, 15, 14, 0) // Monday 2:00 PM
        val overdueHabits1 = dashboardService.getOverdueHabitsList(getHabitsFromDB(mondayAfternoon.date), mondayAfternoon)
        assertTrue(overdueHabits1.isNotEmpty(), "Weekly habit should be overdue when target day time has passed")
        assertEquals(1, overdueHabits1.size)
        assertTrue(overdueHabits1.any { it.name == "Weekly Meeting" })
        
        // Test 2: Today is Monday but time hasn't passed yet
        val mondayMorning = LocalDateTime(2024, 1, 15, 8, 0) // Monday 8:00 AM
        val overdueHabits2 = dashboardService.getOverdueHabitsList(getHabitsFromDB(mondayMorning.date), mondayMorning)
        assertTrue(overdueHabits2.isEmpty(), "Weekly habit should not be overdue when target day time hasn't passed")
        
        // Test 3: Today is Tuesday (day after target day)
        val tuesdayMorning = LocalDateTime(2024, 1, 16, 8, 0) // Tuesday 8:00 AM
        val overdueHabits3 = dashboardService.getOverdueHabitsList(getHabitsFromDB(tuesdayMorning.date), tuesdayMorning)
        assertTrue(overdueHabits3.isNotEmpty(), "Weekly habit should be overdue when target day has passed")
        assertEquals(1, overdueHabits3.size)
        assertTrue(overdueHabits3.any { it.name == "Weekly Meeting" })
        
        // Test 4: Today is Sunday (day before target day)
        val sundayMorning = LocalDateTime(2024, 1, 14, 8, 0) // Sunday 8:00 AM
        val overdueHabits4 = dashboardService.getOverdueHabitsList(getHabitsFromDB(sundayMorning.date), sundayMorning)
        assertTrue(overdueHabits4.isEmpty(), "Weekly habit should not be overdue when target day is in the future")
    }

    @Test
    fun `test monthly habit overdue logic`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // 15th of month 9:00 AM
        
        // Create test habit in database
        createTestHabit("Monthly Review", "MONTHLY", baseDateTime)
        
        // Test 1: Today is 15th and time has passed
        val fifteenthAfternoon = LocalDateTime(2024, 1, 15, 14, 0) // 15th 2:00 PM
        val overdueHabits1 = dashboardService.getOverdueHabitsList(getHabitsFromDB(fifteenthAfternoon.date), fifteenthAfternoon)
        assertTrue(overdueHabits1.isNotEmpty(), "Monthly habit should be overdue when target day time has passed")
        assertEquals(1, overdueHabits1.size)
        assertTrue(overdueHabits1.any { it.name == "Monthly Review" })
        
        // Test 2: Today is 15th but time hasn't passed yet
        val fifteenthMorning = LocalDateTime(2024, 1, 15, 8, 0) // 15th 8:00 AM
        val overdueHabits2 = dashboardService.getOverdueHabitsList(getHabitsFromDB(fifteenthMorning.date), fifteenthMorning)
        assertTrue(overdueHabits2.isEmpty(), "Monthly habit should not be overdue when target day time hasn't passed")
        
        // Test 3: Today is 16th (day after target day)
        val sixteenthMorning = LocalDateTime(2024, 1, 16, 8, 0) // 16th 8:00 AM
        val overdueHabits3 = dashboardService.getOverdueHabitsList(getHabitsFromDB(sixteenthMorning.date), sixteenthMorning)
        assertTrue(overdueHabits3.isNotEmpty(), "Monthly habit should be overdue when target day has passed")
        assertEquals(1, overdueHabits3.size)
        assertTrue(overdueHabits3.any { it.name == "Monthly Review" })
        
        // Test 4: Today is 14th (day before target day)
        val fourteenthMorning = LocalDateTime(2024, 1, 14, 8, 0) // 14th 8:00 AM
        val overdueHabits4 = dashboardService.getOverdueHabitsList(getHabitsFromDB(fourteenthMorning.date), fourteenthMorning)
        assertTrue(overdueHabits4.isEmpty(), "Monthly habit should not be overdue when target day is in the future")
    }

    @Test
    fun `test yearly habit overdue logic`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // January 15th 9:00 AM
        
        // Create test habit in database
        createTestHabit("Yearly Review", "YEARLY", baseDateTime)
        
        // Test 1: Today is January 15th and time has passed
        val jan15Afternoon = LocalDateTime(2024, 1, 15, 14, 0) // Jan 15th 2:00 PM
        val overdueHabits1 = dashboardService.getOverdueHabitsList(getHabitsFromDB(jan15Afternoon.date), jan15Afternoon)
        assertTrue(overdueHabits1.isNotEmpty(), "Yearly habit should be overdue when target day time has passed")
        assertEquals(1, overdueHabits1.size)
        assertTrue(overdueHabits1.any { it.name == "Yearly Review" })
        
        // Test 2: Today is January 15th but time hasn't passed yet
        val jan15Morning = LocalDateTime(2024, 1, 15, 8, 0) // Jan 15th 8:00 AM
        val overdueHabits2 = dashboardService.getOverdueHabitsList(getHabitsFromDB(jan15Morning.date), jan15Morning)
        assertTrue(overdueHabits2.isEmpty(), "Yearly habit should not be overdue when target day time hasn't passed")
        
        // Test 3: Today is January 16th (day after target day)
        val jan16Morning = LocalDateTime(2024, 1, 16, 8, 0) // Jan 16th 8:00 AM
        val overdueHabits3 = dashboardService.getOverdueHabitsList(getHabitsFromDB(jan16Morning.date), jan16Morning)
        assertTrue(overdueHabits3.isNotEmpty(), "Yearly habit should be overdue when target day has passed")
        assertEquals(1, overdueHabits3.size)
        assertTrue(overdueHabits3.any { it.name == "Yearly Review" })
        
        // Test 4: Today is January 14th (day before target day)
        val jan14Morning = LocalDateTime(2024, 1, 14, 8, 0) // Jan 14th 8:00 AM
        val overdueHabits4 = dashboardService.getOverdueHabitsList(getHabitsFromDB(jan14Morning.date), jan14Morning)
        assertTrue(overdueHabits4.isEmpty(), "Yearly habit should not be overdue when target day is in the future")
    }

    @Test
    fun `test done habits are not overdue`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0)
        val currentTime = LocalDateTime(2024, 1, 15, 14, 0) // After the habit time
        
        // Create a done habit
        createTestHabit("Done Habit", "DAILY", baseDateTime, markAsDone = true)
        val habits = getHabitsFromDB(currentTime.date)
        
        println("=== DONE HABITS TEST DEBUG ===")
        println("Test date: ${currentTime.date}")
        habits.forEach { habit ->
            println("Habit: ${habit.name}, Done: ${habit.done}, DateTime: ${habit.dateTime}")
        }
        
        val overdueHabits = dashboardService.getOverdueHabitsList(habits, currentTime)
        println("Overdue habits: ${overdueHabits.size}")
        overdueHabits.forEach { habit ->
            println("  Overdue: ${habit.name} (Done: ${habit.done})")
        }
        println("=== END DONE HABITS TEST DEBUG ===")
        
        assertFalse(overdueHabits.any { it.name == "Done Habit" }, "Done habits should not be considered overdue")
    }

    @Test
    fun `test mixed habits overdue calculation`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0)
        val currentTime = LocalDateTime(2024, 1, 16, 14, 0) // Tuesday 2:00 PM (day after Monday)
        
        // Create different types of habits
        createTestHabit("Daily Exercise", "DAILY", baseDateTime)
        createTestHabit("Weekly Meeting", "WEEKLY", baseDateTime)
        createTestHabit("Monthly Review", "MONTHLY", baseDateTime)
        createTestHabit("Done Habit", "DAILY", baseDateTime, markAsDone = true, doneDate = currentTime.date)
        
        val habits = getHabitsFromDB(currentTime.date)
        
        println("=== MIXED HABITS TEST DEBUG ===")
        println("Test date: ${currentTime.date}")
        habits.forEach { habit ->
            println("Habit: ${habit.name}, Done: ${habit.done}, Frequency: ${habit.frequency}")
        }
        
        val overdueHabits = dashboardService.getOverdueHabitsList(habits, currentTime)
        println("Overdue habits found: ${overdueHabits.size}")
        overdueHabits.forEach { habit ->
            println("  Overdue: ${habit.name} (Done: ${habit.done}, Frequency: ${habit.frequency})")
        }
        println("=== END MIXED HABITS TEST DEBUG ===")
        
        // All three active habits should be overdue (daily, weekly, monthly)
        assertEquals(3, overdueHabits.size, "Should have 3 overdue habits")
        assertTrue(overdueHabits.any { it.name == "Daily Exercise" })
        assertTrue(overdueHabits.any { it.name == "Weekly Meeting" })
        assertTrue(overdueHabits.any { it.name == "Monthly Review" })
        assertFalse(overdueHabits.any { it.name == "Done Habit" })
    }

    @Test
    fun `test get next habit with multiple overdue habits`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // Monday 9:00 AM
        val currentTime = LocalDateTime(2024, 1, 16, 14, 0) // Tuesday 2:00 PM (day after Monday)
        
        // Create multiple habits with different times
        createTestHabit("Early Morning Exercise", "DAILY", LocalDateTime(2024, 1, 15, 6, 0)) // 6:00 AM
        createTestHabit("Weekly Meeting", "WEEKLY", LocalDateTime(2024, 1, 15, 10, 0)) // 10:00 AM
        createTestHabit("Lunch Break", "DAILY", LocalDateTime(2024, 1, 15, 12, 0)) // 12:00 PM
        createTestHabit("Afternoon Walk", "DAILY", LocalDateTime(2024, 1, 15, 16, 0)) // 4:00 PM
        createTestHabit("Evening Reading", "DAILY", LocalDateTime(2024, 1, 15, 20, 0)) // 8:00 PM
        
        val habits = getHabitsFromDB(currentTime.date)
        
        println("=== NEXT HABIT TEST DEBUG ===")
        println("Test date: ${currentTime.date}, Current time: ${currentTime.time}")
        habits.forEach { habit ->
            println("Habit: ${habit.name}, Done: ${habit.done}, Time: ${habit.dateTime}")
        }
        
        val nextHabit = dashboardService.getNextHabit(habits, currentTime)
        println("Next habit: ${nextHabit?.name}")
        
        val overdueHabits = dashboardService.getOverdueHabitsList(habits, currentTime)
        println("Overdue habits: ${overdueHabits.map { it.name }}")
        println("Is next habit overdue: ${overdueHabits.any { it.name == nextHabit?.name }}")
        
        if (nextHabit != null) {
            val isOverdue = overdueHabits.any { it.name == nextHabit.name }
            println("Next habit '${nextHabit.name}' is overdue: $isOverdue")
            if (isOverdue) {
                val overdueHabit = overdueHabits.find { it.name == nextHabit.name }
                println("Overdue habit details: ${overdueHabit?.name}, Done: ${overdueHabit?.done}, Time: ${overdueHabit?.dateTime}")
            }
        }
        println("=== END NEXT HABIT TEST DEBUG ===")
        
        // The next habit should be "Afternoon Walk" (4:00 PM) as it's the closest future time
        assertNotNull(nextHabit, "Should return a next habit")
        assertEquals("Afternoon Walk", nextHabit?.name, "Next habit should be Afternoon Walk (4:00 PM)")
        
        // Verify that the next habit is not overdue
        assertFalse(overdueHabits.any { it.name == nextHabit?.name }, "Next habit should not be overdue")
        
        // Verify that overdue habits are not selected as next habit
        val overdueHabitNames = overdueHabits.map { it.name }
        assertTrue(overdueHabitNames.contains("Early Morning Exercise"), "Early Morning Exercise should be overdue")
        assertTrue(overdueHabitNames.contains("Weekly Meeting"), "Weekly Meeting should be overdue")
        assertTrue(overdueHabitNames.contains("Lunch Break"), "Lunch Break should be overdue")
        assertFalse(overdueHabitNames.contains("Afternoon Walk"), "Afternoon Walk should not be overdue")
        assertFalse(overdueHabitNames.contains("Evening Reading"), "Evening Reading should not be overdue")
    }

    @Test
    fun `test get next habit with all habits overdue`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // Monday 9:00 AM
        val currentTime = LocalDateTime(2024, 1, 16, 22, 0) // Tuesday 10:00 PM (late night)
        
        // Create habits that are all overdue by this time
        createTestHabit("Morning Exercise", "DAILY", LocalDateTime(2024, 1, 15, 6, 0)) // 6:00 AM
        createTestHabit("Weekly Meeting", "WEEKLY", LocalDateTime(2024, 1, 15, 10, 0)) // 10:00 AM
        createTestHabit("Lunch Break", "DAILY", LocalDateTime(2024, 1, 15, 12, 0)) // 12:00 PM
        createTestHabit("Afternoon Walk", "DAILY", LocalDateTime(2024, 1, 15, 16, 0)) // 4:00 PM
        createTestHabit("Evening Reading", "DAILY", LocalDateTime(2024, 1, 15, 20, 0)) // 8:00 PM
        
        val habits = getHabitsFromDB(currentTime.date)
        
        println("=== ALL OVERDUE TEST DEBUG ===")
        println("Test date: ${currentTime.date}, Current time: ${currentTime.time}")
        habits.forEach { habit ->
            println("Habit: ${habit.name}, Done: ${habit.done}, Time: ${habit.dateTime}")
        }
        
        val nextHabit = dashboardService.getNextHabit(habits, currentTime)
        println("Next habit: ${nextHabit?.name}")
        println("=== END ALL OVERDUE TEST DEBUG ===")
        
        // When all habits are overdue, should return null or the closest one for tomorrow
        // This depends on the implementation - let's check what it returns
        if (nextHabit != null) {
            println("Next habit returned: ${nextHabit.name} at ${nextHabit.dateTime}")
            // If it returns a habit, it should be the earliest one for the next occurrence
            assertTrue(nextHabit.name == "Morning Exercise" || nextHabit.name == "Weekly Meeting", 
                "Should return earliest habit for next occurrence")
        } else {
            println("No next habit returned (all overdue)")
        }
        
        // Verify that all habits are indeed overdue
        val overdueHabits = dashboardService.getOverdueHabitsList(habits, currentTime)
        assertEquals(5, overdueHabits.size, "All 5 habits should be overdue")
    }

    @Test
    fun `test get next habit filters out overdue habits`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // Monday 9:00 AM
        val currentTime = LocalDateTime(2024, 1, 16, 14, 0) // Tuesday 2:00 PM (day after Monday)
        
        // Create habits with different times - some will be overdue, some won't
        createTestHabit("Overdue Morning", "DAILY", LocalDateTime(2024, 1, 15, 6, 0)) // 6:00 AM - overdue
        createTestHabit("Overdue Weekly", "WEEKLY", LocalDateTime(2024, 1, 15, 10, 0)) // 10:00 AM - overdue
        createTestHabit("Overdue Lunch", "DAILY", LocalDateTime(2024, 1, 15, 12, 0)) // 12:00 PM - overdue
        createTestHabit("Future Afternoon", "DAILY", LocalDateTime(2024, 1, 15, 16, 0)) // 4:00 PM - future
        createTestHabit("Future Evening", "DAILY", LocalDateTime(2024, 1, 15, 20, 0)) // 8:00 PM - future
        
        val habits = getHabitsFromDB(currentTime.date)
        
        println("=== FILTER OVERDUE TEST DEBUG ===")
        println("Test date: ${currentTime.date}, Current time: ${currentTime.time}")
        habits.forEach { habit ->
            println("Habit: ${habit.name}, Done: ${habit.done}, Time: ${habit.dateTime}")
        }
        
        val nextHabit = dashboardService.getNextHabit(habits, currentTime)
        println("Next habit: ${nextHabit?.name}")
        
        val overdueHabits = dashboardService.getOverdueHabitsList(habits, currentTime)
        println("Overdue habits: ${overdueHabits.map { it.name }}")
        println("=== END FILTER OVERDUE TEST DEBUG ===")
        
        // Verify that a next habit is returned
        assertNotNull(nextHabit, "Should return a next habit")
        
        // Verify that the next habit is NOT in the overdue list
        assertFalse(overdueHabits.any { it.name == nextHabit?.name }, 
            "Next habit '${nextHabit?.name}' should not be overdue")
        
        // Verify that the next habit is one of the future habits
        val expectedFutureHabits = listOf("Future Afternoon", "Future Evening")
        assertTrue(expectedFutureHabits.contains(nextHabit?.name), 
            "Next habit should be one of the future habits: $expectedFutureHabits")
        
        // Verify that overdue habits are not selected
        val overdueHabitNames = overdueHabits.map { it.name }
        val expectedOverdueHabits = listOf("Overdue Morning", "Overdue Weekly", "Overdue Lunch")
        expectedOverdueHabits.forEach { overdueHabit ->
            assertTrue(overdueHabitNames.contains(overdueHabit), 
                "$overdueHabit should be marked as overdue")
            assertNotEquals(overdueHabit, nextHabit?.name, 
                "Overdue habit '$overdueHabit' should not be selected as next habit")
        }
        
        // Verify that future habits are not overdue
        val futureHabitNames = listOf("Future Afternoon", "Future Evening")
        futureHabitNames.forEach { futureHabit ->
            assertFalse(overdueHabitNames.contains(futureHabit), 
                "Future habit '$futureHabit' should not be overdue")
        }
    }

    @Test
    fun `test get next habit with mixed frequencies and overdue filtering`() {
        val baseDateTime = LocalDateTime(2024, 1, 15, 9, 0) // Monday 9:00 AM
        val currentTime = LocalDateTime(2024, 1, 16, 14, 0) // Tuesday 2:00 PM (day after Monday)
        
        // Create habits with different frequencies and times
        // DAILY habits - some overdue, some future
        createTestHabit("Daily Overdue 6AM", "DAILY", LocalDateTime(2024, 1, 15, 6, 0)) // 6:00 AM - overdue
        createTestHabit("Daily Overdue 12PM", "DAILY", LocalDateTime(2024, 1, 15, 12, 0)) // 12:00 PM - overdue
        createTestHabit("Daily Future 4PM", "DAILY", LocalDateTime(2024, 1, 15, 16, 0)) // 4:00 PM - future
        createTestHabit("Daily Future 8PM", "DAILY", LocalDateTime(2024, 1, 15, 20, 0)) // 8:00 PM - future
        
        // WEEKLY habits - some overdue, some future
        createTestHabit("Weekly Overdue Monday", "WEEKLY", LocalDateTime(2024, 1, 15, 10, 0)) // Monday 10:00 AM - overdue
        createTestHabit("Weekly Future Friday", "WEEKLY", LocalDateTime(2024, 1, 19, 14, 0)) // Friday 2:00 PM - future
        
        // MONTHLY habits - some overdue, some future
        createTestHabit("Monthly Overdue 5th", "MONTHLY", LocalDateTime(2024, 1, 5, 9, 0)) // 5th 9:00 AM - overdue
        createTestHabit("Monthly Future 20th", "MONTHLY", LocalDateTime(2024, 1, 20, 15, 0)) // 20th 3:00 PM - future
        
        val habits = getHabitsFromDB(currentTime.date)
        
        println("=== MIXED FREQUENCIES TEST DEBUG ===")
        println("Test date: ${currentTime.date}, Current time: ${currentTime.time}")
        habits.forEach { habit ->
            println("Habit: ${habit.name}, Done: ${habit.done}, Frequency: ${habit.frequency}, Time: ${habit.dateTime}")
        }
        
        val nextHabit = dashboardService.getNextHabit(habits, currentTime)
        println("Next habit: ${nextHabit?.name}")
        
        val overdueHabits = dashboardService.getOverdueHabitsList(habits, currentTime)
        println("Overdue habits: ${overdueHabits.map { it.name }}")
        println("=== END MIXED FREQUENCIES TEST DEBUG ===")
        
        // Verify that a next habit is returned
        assertNotNull(nextHabit, "Should return a next habit")
        
        // Verify that the next habit is NOT in the overdue list
        assertFalse(overdueHabits.any { it.name == nextHabit?.name }, 
            "Next habit '${nextHabit?.name}' should not be overdue")
        
        // Verify that the next habit is one of the expected future habits
        val expectedFutureHabits = listOf(
            "Daily Future 4PM", "Daily Future 8PM", 
            "Weekly Future Friday", 
            "Monthly Future 20th"
        )
        assertTrue(expectedFutureHabits.contains(nextHabit?.name), 
            "Next habit should be one of the future habits: $expectedFutureHabits")
        
        // Verify that overdue habits are not selected
        val overdueHabitNames = overdueHabits.map { it.name }
        val expectedOverdueHabits = listOf(
            "Daily Overdue 6AM", "Daily Overdue 12PM",
            "Weekly Overdue Monday", 
            "Monthly Overdue 5th"
        )
        expectedOverdueHabits.forEach { overdueHabit ->
            assertTrue(overdueHabitNames.contains(overdueHabit), 
                "$overdueHabit should be marked as overdue")
            assertNotEquals(overdueHabit, nextHabit?.name, 
                "Overdue habit '$overdueHabit' should not be selected as next habit")
        }
        
        // Verify that future habits are not overdue
        expectedFutureHabits.forEach { futureHabit ->
            assertFalse(overdueHabitNames.contains(futureHabit), 
                "Future habit '$futureHabit' should not be overdue")
        }
        
        // Verify that the next habit is the closest future one
        // Based on the test setup, "Daily Future 4PM" should be the closest
        assertEquals("Daily Future 4PM", nextHabit?.name, 
            "Next habit should be the closest future habit (Daily Future 4PM)")
    }

    @Test
    fun `test getNextHabit returns Meditate from realistic scenario`() {
        val currentTime = LocalDateTime(2024, 1, 16, 20, 15) // 20:15, just before Meditate at 20:30

        // Overdue habits (should be filtered out)
        createTestHabit("Do Chores (Sweeping and tidying)", "WEEKLY", LocalDateTime(2024, 1, 15, 9, 0)) // Monday
        createTestHabit("Check Budget", "WEEKLY", LocalDateTime(2024, 1, 15, 9, 0)) // Monday
        createTestHabit("Study", "DAILY", LocalDateTime(2024, 1, 16, 14, 0))
        createTestHabit("Brush teeth", "DAILY", LocalDateTime(2024, 1, 16, 14, 0))
        createTestHabit("Make lunch for tomorrow", "DAILY", LocalDateTime(2024, 1, 16, 17, 0))

        // Pending/future habits (should be considered for next)
        createTestHabit("Prepare my day", "DAILY", LocalDateTime(2024, 1, 16, 19, 0))
        createTestHabit("Brush teeth", "DAILY", LocalDateTime(2024, 1, 16, 20, 0))
        createTestHabit("Meditate", "DAILY", LocalDateTime(2024, 1, 16, 20, 30))

        // Use the real dashboard service to get the next habit and overdue habits
        val dashboardData = dashboardService.getDashboardData(userId, currentTime.formatDefault())
        val nextHabit = dashboardData.nextHabit
        val overdueHabits = dashboardData.overdueHabitsList

        println("Next habit: ${nextHabit?.name}")
        println("Overdue habits: ${overdueHabits.map { it.name }}")

        // Should return "Meditate" as the next habit
        assertNotNull(nextHabit, "Should return a next habit")
        assertEquals("Meditate", nextHabit?.name, "Next habit should be Meditate (20:30)")
        // Should not be overdue
        assertFalse(overdueHabits.any { it.name == nextHabit?.name }, "Next habit should not be overdue")
        // All overdue habits should be in the overdue list
        val expectedOverdue = listOf(
            "Do Chores (Sweeping and tidying)", "Check Budget", "Study", "Brush teeth", "Make lunch for tomorrow"
        )
        expectedOverdue.forEach { name ->
            assertTrue(overdueHabits.any { it.name == name }, "$name should be overdue")
        }
    }

    @Test
    fun `test getDashboardData returns correct next and overdue habits`() {
        val currentTime = LocalDateTime(2024, 1, 16, 20, 15) // 20:15, just before Meditate at 20:30

        // Overdue habits (should be filtered out)
        createTestHabit("Do Chores (Sweeping and tidying)", "WEEKLY", LocalDateTime(2024, 1, 15, 9, 0)) // Monday
        createTestHabit("Check Budget", "WEEKLY", LocalDateTime(2024, 1, 15, 9, 0)) // Monday
        createTestHabit("Study", "DAILY", LocalDateTime(2024, 1, 16, 14, 0))
        createTestHabit("Brush teeth", "DAILY", LocalDateTime(2024, 1, 16, 14, 0))
        createTestHabit("Make lunch for tomorrow", "DAILY", LocalDateTime(2024, 1, 16, 17, 0))

        // Pending/future habits (should be considered for next)
        createTestHabit("Prepare my day", "DAILY", LocalDateTime(2024, 1, 16, 19, 0))
        createTestHabit("Brush teeth", "DAILY", LocalDateTime(2024, 1, 16, 20, 0))
        createTestHabit("Meditate", "DAILY", LocalDateTime(2024, 1, 16, 20, 30))

        // Call dashboard data
        val dashboardData = dashboardService.getDashboardData(userId, currentTime.formatDefault())
        val nextHabit = dashboardData.nextHabit
        val overdueHabits = dashboardData.overdueHabitsList

        println("Dashboard next habit: ${nextHabit?.name}")
        println("Dashboard overdue habits: ${overdueHabits.map { it.name }}")

        // Should return "Meditate" as the next habit
        assertNotNull(nextHabit, "Should return a next habit in dashboard data")
        assertEquals("Meditate", nextHabit?.name, "Next habit in dashboard should be Meditate (20:30)")
        // Should not be overdue
        assertFalse(overdueHabits.any { it.name == nextHabit?.name }, "Next habit should not be overdue in dashboard")
        // All overdue habits should be in the overdue list
        val expectedOverdue = listOf(
            "Do Chores (Sweeping and tidying)", "Check Budget", "Study", "Brush teeth", "Make lunch for tomorrow"
        )
        expectedOverdue.forEach { name ->
            assertTrue(overdueHabits.any { it.name == name }, "$name should be overdue in dashboard")
        }
    }

    private fun createTestHabit(
        name: String, 
        frequency: String, 
        dateTime: LocalDateTime, 
        markAsDone: Boolean = false,
        doneDate: LocalDate? = null
    ): UUID {
        return transaction {
            val habit = Habit.new {
                this.name = name
                this.frequency = frequency
                this.baseDateTime = dateTime
                this.user = com.esteban.ruano.database.entities.User[userId]
                this.status = Status.ACTIVE
                this.note = "Test habit"
            }
            
            if (markAsDone) {
                // Mark as done by creating a track record
                val trackDateTime = if (doneDate != null) {
                    LocalDateTime(doneDate, dateTime.time)
                } else {
                    dateTime
                }
                
                com.esteban.ruano.database.entities.HabitTrack.new {
                    this.habit = Habit[habit.id]
                    doneDateTime = trackDateTime
                    status = Status.ACTIVE
                }
            }
            
            habit.id.value
        }
    }

    private fun getHabitsFromDB(testDate: LocalDate): List<HabitDTO> {
        // Use a simpler approach for testing that includes done habits
        return transaction {
            val habits = Habit.find { Habits.user eq userId }.toList()
            val result = mutableListOf<HabitDTO>()
            
            for (habit in habits) {
                val tracking = HabitTrack.find {
                    (HabitTracks.habitId eq habit.id) and (HabitTracks.status eq Status.ACTIVE)
                }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

                val dto = habit.toHabitDTO()
                // Use the base date to determine if done
                val isDone = HabitUtils.isDone(dto, tracking, testDate)
                
                println("=== HABIT DONE CHECK DEBUG ===")
                println("Habit: ${habit.name}")
                println("Test date: $testDate")
                println("Tracking: ${tracking?.doneDateTime}")
                println("Tracking date: ${tracking?.doneDateTime?.date}")
                println("Is done: $isDone")
                println("=== END HABIT DONE CHECK DEBUG ===")
                
                result.add(dto.copy(done = isDone))
            }
            
            result
        }
    }
} 