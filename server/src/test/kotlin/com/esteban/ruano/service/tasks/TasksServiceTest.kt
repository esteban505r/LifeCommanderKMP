package lopez.esteban.com.tasks

import com.esteban.ruano.database.entities.HistoryTracks
import com.esteban.ruano.database.entities.Reminders
import com.esteban.ruano.database.entities.Tasks
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.tasks.UpdateTaskDTO
import com.esteban.ruano.service.ReminderService
import com.esteban.ruano.service.TaskService
import com.esteban.ruano.utils.DateUtils.formatDateTime
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class TasksServiceTest {


    private lateinit var service : TaskService

    @Before
    fun setup() {
        //Ensure you have a postgres database running on localhost:5431 with the name testdb
        service = TaskService(ReminderService())
        Database.connect(
            url = "jdbc:postgresql://localhost:5431/testdb",
            driver = "org.postgresql.Driver",
            user = "testuser",
            password = "testpassword"
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timeZone = TimeZone.currentSystemDefault()
        val yesterdayInstant = Clock.System.now() - 1L.toDuration(DurationUnit.DAYS)
        val yesterdayDateTime = yesterdayInstant.toLocalDateTime(timeZone)
        val dayBeforeYesterdayInstant = Clock.System.now() - 2L.toDuration(DurationUnit.DAYS)
        val dayBeforeYesterdayTime = dayBeforeYesterdayInstant.toLocalDateTime(timeZone)

        transaction {
            Tasks.deleteAll()
            Reminders.deleteAll()
            Users.deleteAll()
        }

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                Tasks,
                Reminders,
                HistoryTracks
            )
        }

        transaction {
            Users.insert {
                it[id] = 1
                it[name] = "Test"
                it[email] = "test@test.com"
                it[password] = "password"
            }
        }

        transaction {
            Tasks.insert {
                it[name] = "Test Task"
                it[note] = "Test Note"
                it[doneDateTime] = yesterdayDateTime
                it[dueDateTime] = currentDate
                it[user] = 1
            }
        }

        transaction {
            Tasks.insert {
                it[name] = "Test Task 2"
                it[note] = "Test Note 2"
                it[doneDateTime] = dayBeforeYesterdayTime
                it[dueDateTime] = currentDate
                it[user] = 1
            }
        }

        transaction {
            Tasks.insert {
                it[name] = "Test Task 3 (Not Done)"
                it[note] = "Test Note 3"
                it[doneDateTime] = null
                it[dueDateTime] = currentDate
                it[user] = 1
            }
        }

        transaction {
            Tasks.insert {
                it[name] = "Test Task 4 (Not Done) (Scheduled)"
                it[note] = "Test Note 4"
                it[doneDateTime] = null
                it[scheduledDateTime] = currentDate
                it[dueDateTime] = null
                it[user] = 1
            }
        }

    }

    @Test
    fun `should get tasks by user id`() {
        assert(service.getByUserId(1,30,0).isNotEmpty())
    }

    @Test
    fun `should not get tasks by user id`() {
        assert(service.getByUserId(2,30,0).isEmpty())
    }

    @Test
    fun `should not get second done task because its hidden time`() {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val tasks = service.fetchAll(1,"",30,0,currentDate)
        assert(tasks.size == 3)
    }

    @Test
    fun `should get second done task because its hidden time`() {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val tasks = service.fetchAllByDateRange(1,"",date.minus(1,DateTimeUnit.DAY),date,30,0, doneTaskHiddenTimeInDays = 2)
        assert(tasks.size == 4)
    }

    @Test
    fun `should create a new task`() {
        val createTaskDTO = CreateTaskDTO(
            name = "New Test Task",
            note = "New Test Note",
            dueDateTime = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())),
            priority = 1
        )
        
        val taskId = service.create(1, createTaskDTO)
        assert(taskId != null)
        
        val createdTask = service.getByIdAndUserId(taskId!!, 1)
        assert(createdTask != null)
        assert(createdTask!!.name == "New Test Task")
        assert(createdTask.note == "New Test Note")
        assert(createdTask.dueDateTime != null)
    }

    @Test
    fun `should update an existing task`() {
        val createTaskDTO = CreateTaskDTO(
            name = "Task to Update",
            note = "Original Note",
            dueDateTime = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        
        val taskId = service.create(1, createTaskDTO)
        assert(taskId != null)
        
        val updateTaskDTO = UpdateTaskDTO(
            name = "Updated Task Name",
            note = "Updated Note",
            dueDateTime = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        
        val wasUpdated = service.update(1, taskId!!, updateTaskDTO)
        assert(wasUpdated)
        
        val updatedTask = service.getByIdAndUserId(taskId, 1)
        assert(updatedTask != null)
        assert(updatedTask!!.name == "Updated Task Name")
        assert(updatedTask.note == "Updated Note")
        assert(updatedTask.dueDateTime != null)
    }

    @Test
    fun `should delete a task`() {
        val createTaskDTO = CreateTaskDTO(
            name = "Task to Delete",
            note = "Delete Note",
            dueDateTime = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        
        val taskId = service.create(1, createTaskDTO)
        assert(taskId != null)
        
        val wasDeleted = service.delete(1, taskId!!)
        assert(wasDeleted)
        
        val deletedTask = service.getByIdAndUserId(taskId, 1)
        assert(deletedTask == null)
    }

    @Test
    fun `should not delete task of different user`() {
        val createTaskDTO = CreateTaskDTO(
            name = "Task to Delete",
            note = "Delete Note",
            dueDateTime = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        
        val taskId = service.create(1, createTaskDTO)
        assert(taskId != null)
        
        val wasDeleted = try {
            service.delete(2, taskId!!)
        } catch (e: Exception) {
            false
        }
        assert(!wasDeleted)
        
        val task = taskId?.let { service.getByIdAndUserId(taskId, 1)}
        assert(task != null)
    }

    @Test
    fun `should update task scheduled date`() {
        val createTaskDTO = CreateTaskDTO(
            name = "Task with Schedule",
            note = "Schedule Note",
            dueDateTime = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        
        val taskId = service.create(1, createTaskDTO)
        assert(taskId != null)
        
        val currentInstant = Clock.System.now()
        val tomorrowInstant = currentInstant.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        val tomorrow = tomorrowInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        val updateTaskDTO = UpdateTaskDTO(
            scheduledDateTime = formatDateTime(tomorrow)
        )
        
        val wasUpdated = service.update(1, taskId!!, updateTaskDTO)
        assert(wasUpdated)
        
        val updatedTask = service.getByIdAndUserId(taskId, 1)
        assert(updatedTask != null)
        assert(updatedTask!!.scheduledDateTime != null)
        assert(updatedTask.scheduledDateTime == formatDateTime(tomorrow))
    }
}