package com.esteban.ruano.service.tasks

import com.esteban.ruano.BaseTest
import com.esteban.ruano.database.entities.Task
import com.esteban.ruano.database.entities.TaskTrack
import com.esteban.ruano.database.entities.TaskTracks
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.models.Priority
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.service.TaskService
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
import org.junit.Test
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskTrackServiceTest : BaseTest() {
    private val taskService: TaskService by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.Companion.create {
        printLogger()
        modules(testModule)
    }

    @BeforeTest
    override fun setup() {
        super.setup()
    }


    @Test
    fun `test create task track`() {
        val taskId = createTestTask()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).formatDefault().toLocalDateTimeUI()

        val success = taskService.completeTask(userId, taskId, doneDateTime.formatDefault())
        assertTrue(success)

        transaction {
            val track = TaskTrack.Companion.find { TaskTracks.taskId eq taskId }.firstOrNull()
            assertNotNull(track)
            assertEquals(taskId, track.task.id.value)
            assertEquals(doneDateTime, track.doneDateTime)
            assertEquals(Status.ACTIVE, track.status)
        }
    }

    @Test
    fun `test get tasks completed per day this week`() {
        // Create tasks and complete them on different days
        val task1 = createTestTask()
        val task2 = createTestTask()
        val task3 = createTestTask()

        val today = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).date
        val monday = today.minus((today.dayOfWeek.ordinal).toLong(), DateTimeUnit.Companion.DAY)

        // Complete tasks on Monday, Wednesday, and Friday
        taskService.completeTask(userId, task1, monday.atTime(10, 0).formatDefault())
        taskService.completeTask(userId, task2, monday.plus(2, DateTimeUnit.Companion.DAY).atTime(10, 0).formatDefault())
        taskService.completeTask(userId, task3, monday.plus(4, DateTimeUnit.Companion.DAY).atTime(10, 0).formatDefault())

        val completedPerDay = taskService.getTasksCompletedPerDayThisWeek(userId)

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
    fun `test uncomplete task removes latest track`() {
        val taskId = createTestTask()
        val doneDateTime = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())

        // Complete task
        taskService.completeTask(userId, taskId, doneDateTime.formatDefault())

        // Uncomplete task
        val success = taskService.unCompleteTask(userId, taskId)
        assertTrue(success)

        transaction {
            val track = TaskTrack.Companion.find { TaskTracks.taskId eq taskId }.firstOrNull()
            assertNotNull(track)
            assertEquals(Status.INACTIVE, track.status)
        }
    }

    private fun createTestTask(): UUID {
        return transaction {
            val task = Task.Companion.new {
                user = User[userId]
                name = "Test Task ${UUID.randomUUID()}"
                priority = Priority.MEDIUM
                note = "This is a test task"
                status = Status.ACTIVE
            }
            task.id.value
        }
    }
}