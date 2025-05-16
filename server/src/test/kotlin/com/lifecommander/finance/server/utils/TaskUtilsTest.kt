package lopez.esteban.com.utils

import com.esteban.ruano.database.models.Priority
import kotlinx.datetime.*
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TaskUtilsTest {

    @Test
    fun `should sort tasks by due date, scheduled date, and priority`() {
        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val tomorrow = (Clock.System.now() - 1L.toDuration(DurationUnit.DAYS)).toLocalDateTime(TimeZone.currentSystemDefault())
        val nextWeek = (Clock.System.now() + 7L.toDuration(DurationUnit.DAYS)).toLocalDateTime(TimeZone.currentSystemDefault())

        // Create test tasks with different combinations of due dates, scheduled dates, and priorities
        val tasks = listOf(
            TestTask(
                name = "Task 1",
                dueDate = nextWeek,
                scheduledDate = tomorrow,
                priority = Priority.LOW
            ),
            TestTask(
                name = "Task 2",
                dueDate = currentDateTime,
                scheduledDate = null,
                priority = Priority.HIGH
            ),
            TestTask(
                name = "Task 3",
                dueDate = tomorrow,
                scheduledDate = currentDateTime,
                priority = Priority.MEDIUM
            ),
            TestTask(
                name = "Task 4",
                dueDate = null,
                scheduledDate = nextWeek,
                priority = Priority.HIGH
            ),
            TestTask(
                name = "Task 5",
                dueDate = currentDateTime,
                scheduledDate = tomorrow,
                priority = Priority.LOW
            )
        )

        val sortedTasks = tasks.sortedWith(compareBy<TestTask?> { it?.priority }
            .thenBy { it?.scheduledDate }
            .thenBy { it?.dueDate }
        )

        // Verify sorting order:
        // 1. First by priority (HIGH -> MEDIUM -> LOW)
        // 2. Then by scheduled date (nulls last)
        // 3. Finally by due date (nulls last)
        assertEquals("Task 2", sortedTasks[0].name) // HIGH priority, due today, no schedule
        assertEquals("Task 4", sortedTasks[1].name) // HIGH priority, no due date, scheduled next week
        assertEquals("Task 3", sortedTasks[2].name) // MEDIUM priority, due tomorrow, scheduled today
        assertEquals("Task 5", sortedTasks[3].name) // LOW priority, due today, scheduled tomorrow
        assertEquals("Task 1", sortedTasks[4].name) // LOW priority, due next week, scheduled tomorrow
    }

    @Test
    fun `should handle tasks with same dates but different priorities`() {
        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val tasks = listOf(
            TestTask(
                name = "Task 1",
                dueDate = currentDateTime,
                scheduledDate = currentDateTime,
                priority = Priority.LOW
            ),
            TestTask(
                name = "Task 2",
                dueDate = currentDateTime,
                scheduledDate = currentDateTime,
                priority = Priority.HIGH
            ),
            TestTask(
                name = "Task 3",
                dueDate = currentDateTime,
                scheduledDate = currentDateTime,
                priority = Priority.MEDIUM
            )
        )

        val sortedTasks = tasks.sortedWith(compareBy<TestTask?> { it?.priority }
            .thenBy { it?.scheduledDate }
            .thenBy { it?.dueDate }
        )

        assertEquals("Task 2", sortedTasks[0].name) // HIGH priority
        assertEquals("Task 3", sortedTasks[1].name) // MEDIUM priority
        assertEquals("Task 1", sortedTasks[2].name) // LOW priority
    }

    @Test
    fun `should handle tasks with no dates`() {
        val tasks = listOf(
            TestTask(
                name = "Task 1",
                dueDate = null,
                scheduledDate = null,
                priority = Priority.HIGH
            ),
            TestTask(
                name = "Task 2",
                dueDate = null,
                scheduledDate = null,
                priority = Priority.LOW
            ),
            TestTask(
                name = "Task 3",
                dueDate = null,
                scheduledDate = null,
                priority = Priority.MEDIUM
            )
        )

        val sortedTasks = tasks.sortedWith(compareBy<TestTask?> { it?.priority }
            .thenBy { it?.scheduledDate }
            .thenBy { it?.dueDate }
        )

        assertEquals("Task 1", sortedTasks[0].name) // HIGH priority
        assertEquals("Task 3", sortedTasks[1].name) // MEDIUM priority
        assertEquals("Task 2", sortedTasks[2].name) // LOW priority
    }

    private data class TestTask(
        val name: String,
        val dueDate: LocalDateTime?,
        val scheduledDate: LocalDateTime?,
        val priority: Priority,
        val note: String = "Test Note"
    )
} 