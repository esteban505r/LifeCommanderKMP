package com.esteban.ruano.service

import kotlinx.datetime.*
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Task
import com.esteban.ruano.database.entities.Tasks
import com.esteban.ruano.database.entities.TaskTrack
import com.esteban.ruano.database.entities.TaskTracks
import com.esteban.ruano.database.models.Priority.Companion.toPriority
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.reminders.UpdateReminderDTO
import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.tasks.UpdateTaskDTO
import com.esteban.ruano.utils.sortedByDefault
import com.esteban.ruano.utils.fromDateToLong
import com.esteban.ruano.utils.parseDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class TaskService(
    val reminderService: ReminderService
) : BaseService() {

    data class TaskNotificationInfo(
        val overdueTasks: List<Task>,
        val dueTodayTasks: List<Task>,
        val scheduledTodayTasks: List<Task>
    )

    fun getDueTasksForNotification(userId: Int, today: LocalDate): TaskNotificationInfo {
        return transaction {
            // Get all active tasks for this user that are not completed
            val allActiveTasks = Task.find {
                (Tasks.status eq Status.ACTIVE) and
                        (Tasks.doneDateTime eq null) and
                        (Tasks.user eq userId)
            }.toList()

            val overdueTasks = mutableListOf<Task>()
            val dueTodayTasks = mutableListOf<Task>()
            val scheduledTodayTasks = mutableListOf<Task>()

            for (task in allActiveTasks) {
                when {
                    // Overdue tasks: due date is before today
                    task.dueDate != null && task.dueDate!!.date < today -> {
                        overdueTasks.add(task)
                    }
                    // Due today tasks: due date is today
                    task.dueDate != null && task.dueDate!!.date == today -> {
                        dueTodayTasks.add(task)
                    }
                    // Scheduled today tasks: scheduled date is today (but not due today)
                    task.scheduledDate != null && task.scheduledDate!!.date == today &&
                            (task.dueDate == null || task.dueDate!!.date != today) -> {
                        scheduledTodayTasks.add(task)
                    }
                }
            }

            TaskNotificationInfo(overdueTasks, dueTodayTasks, scheduledTodayTasks)
        }
    }

    fun shouldSendTaskNotification(
        userId: Int,
        lastNotificationTime: Long,
        notificationFrequencyMinutes: Int
    ): Boolean {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val timeSinceLastNotification = currentTimeMillis - lastNotificationTime
        val notificationIntervalMs = notificationFrequencyMinutes * 60 * 1000L

        return timeSinceLastNotification >= notificationIntervalMs
    }

    fun create(userId: Int, task: CreateTaskDTO): UUID? {
        return transaction {
            val id = Tasks.insertOperation(userId, task.createdAt?.fromDateToLong()) {
                insert {
                    it[name] = task.name
                    it[doneDateTime] = task.doneDateTime?.let { parseDateTime(task.doneDateTime) }
                    it[scheduledDateTime] = task.scheduledDateTime?.let { parseDateTime(task.scheduledDateTime) }
                    it[priority] = task.priority.toPriority()
                    it[note] = task.note
                    it[dueDateTime] = if (task.dueDateTime != null) parseDateTime(task.dueDateTime) else null
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id?.let {
                val currentReminders = reminderService.getByHabitID(id)
                val remindersToDelete = currentReminders.filter {
                    task.reminders?.firstOrNull { reminder -> reminder.id == it.id.toString() } == null
                }
                remindersToDelete.forEach {
                    reminderService.delete(userId, it.id.value)
                }
                task.reminders?.forEach { reminder ->
                    reminderService.create(userId, reminder.copy(taskId = it.toString()))
                }
            }
            id
        }
    }

    fun update(userId: Int, id: UUID, task: UpdateTaskDTO): Boolean {
        return transaction {
            val updatedRow = Tasks.updateOperation(userId, task.updatedAt?.fromDateToLong()) {
                val updatedRows = update({ (Tasks.id eq id) }) { row ->
                    task.name?.let { row[name] = it }
                    task.note?.let { row[note] = it }
                    task.priority?.let { row[priority] = it.toPriority() }
                    task.scheduledDateTime?.let { row[scheduledDateTime] = parseDateTime(it) }
                    task.doneDateTime?.let { row[doneDateTime] = parseDateTime(it) }
                    task.dueDateTime?.let { row[dueDateTime] = parseDateTime(it) }

                    // Handle fields that should be cleared
                    task.clearFields?.forEach { fieldName ->
                        when (fieldName.lowercase()) {
                            "duedatetime" -> row[dueDateTime] = null
                            "scheduleddatetime" -> row[scheduledDateTime] = null
                            "donedatetime" -> row[doneDateTime] = null
                            "note" -> row[note] = ""
                            "name" -> row[name] = ""
                        }
                    }
                }
                id.let {
                    task.reminders?.forEach { reminder ->
                        if (reminder.id != null) reminderService.update(
                            userId, UUID.fromString(reminder.id), UpdateReminderDTO(
                                id = reminder.id,
                                taskId = id.toString(),
                                time = reminder.time
                            )
                        )
                        else {
                            reminderService.create(userId, reminder.copy(taskId = it.toString()))
                        }
                    }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = Tasks.deleteOperation(userId) {
                val updatedRows = Tasks.update({ (Tasks.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }


    fun getByUserId(userId: Int, limit: Int, offset: Long): List<TaskDTO> {
        return transaction {
            Task.find {
                (Tasks.user eq userId) and (Tasks.status eq Status.ACTIVE)
            }.limit(limit, offset).toList().sortedByDefault().map { it.toDTO() }
        }
    }


    fun fetchAll(
        userId: Int,
        pattern: String,
        limit: Int,
        offset: Long,
        date: LocalDate,
        withOverdue: Boolean = true
    ): List<TaskDTO> {
        return fetchAllByDateRange(
            userId,
            pattern,
            date,
            date,
            limit,
            offset,
            withOverdue
        )
    }

    fun fetchAll(userId: Int, pattern: String, limit: Int, offset: Long): List<TaskDTO> {
        return transaction {
            val tasks =
                Task.find { (Tasks.user eq userId) and (Tasks.name.lowerCase() like "%${pattern.lowercase()}%") and (Tasks.status eq Status.ACTIVE) }
                    .limit(limit, offset).toList().sortedByDefault().map { it.toDTO() }
            tasks.map {
                val reminders = reminderService.getByTaskID(
                    UUID.fromString(it.id)
                )
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

    fun fetchAllByDateRange(
        userId: Int,
        pattern: String,
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        offset: Long,
        withOverdue: Boolean = true,
        doneTaskHiddenTimeInDays: Long = 1
    ): List<TaskDTO> {
        val query =
            if (withOverdue)
                ((Tasks.dueDateTime.date() greaterEq startDate)
                    .or((Tasks.dueDateTime.date() lessEq startDate) and (Tasks.doneDateTime eq null)))
                    .or(Tasks.scheduledDateTime.date() greaterEq startDate)
                    .or((Tasks.scheduledDateTime.date() lessEq startDate) and (Tasks.doneDateTime eq null))
            else (Tasks.dueDateTime.date() greaterEq startDate) or (Tasks.scheduledDateTime.date() greaterEq startDate)

        return transaction {
            val tasks = Task.find {
                (Tasks.user eq userId) and (Tasks.name.lowerCase() like "%${pattern.lowercase()}%")
                    // Check if dueDateTime falls after startDate
                    .and(query)
                    // Also if scheduledDateTime falls between startDate and endDate
                    .or(Tasks.scheduledDateTime.date() lessEq endDate)
                    // Check the state
                    .and(Tasks.status eq Status.ACTIVE)
                    // Check the done task should be hidden based on the doneTaskHiddenTimeInDays
                    .and(
                        (Tasks.doneDateTime eq null) or (Tasks.doneDateTime.date() greaterEq endDate.minus(
                            doneTaskHiddenTimeInDays,
                            DateTimeUnit.DAY
                        ))
                    )

            }
                .limit(limit, offset).toList().sortedByDefault().map { it.toDTO() }
            tasks.map {
                val reminders = reminderService.getByTaskID(
                    UUID.fromString(it.id)
                )
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

    fun fetchAllNoDueDate(userId: Int, pattern: String, limit: Int, offset: Long): List<TaskDTO> {
        return transaction {
            val tasks =
                Task.find { (Tasks.user eq userId) and (Tasks.name.lowerCase() like "%${pattern.lowercase()}%") and (Tasks.dueDateTime eq null) and (Tasks.status eq Status.ACTIVE) }
                    .limit(limit, offset).toList().sortedByDefault().map { it.toDTO() }
            tasks.map {
                val reminders = reminderService.getByTaskID(
                    UUID.fromString(it.id)
                )
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

    fun fetchAllByDateRangeWithSmartFiltering(
        userId: Int,
        pattern: String,
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        offset: Long,
        isTodayFilter: Boolean = false,
        doneTaskHiddenTimeInDays: Long = 1
    ): List<TaskDTO> {
        return transaction {
            val query = if (isTodayFilter) {
                // For Today filter: 
                // - Show all due tasks (overdue, today, future) because they should be done as soon as possible
                // - Show only scheduled tasks for today (not future scheduled tasks)
                ((Tasks.dueDateTime.date() lessEq startDate) or
                        ((Tasks.dueDateTime.date() greaterEq startDate)) and
                        (Tasks.doneDateTime eq null))  // Due today or overdue
                    .or((Tasks.scheduledDateTime.date() eq startDate) and (Tasks.doneDateTime eq null))  // Scheduled for today only
            } else {
                // For other filters: only show scheduled tasks for the date range

                (Tasks.scheduledDateTime.date() greaterEq startDate) and
                        (Tasks.scheduledDateTime.date() lessEq endDate) and
                        (Tasks.doneDateTime eq null)
            }

            val tasks = Task.find {
                (Tasks.user eq userId) and
                        (Tasks.name.lowerCase() like "%${pattern.lowercase()}%") and
                        query and
                        (Tasks.status eq Status.ACTIVE)
            }.orderBy(Tasks.dueDateTime to SortOrder.ASC_NULLS_LAST).orderBy(
                Tasks.scheduledDateTime to SortOrder.ASC_NULLS_LAST
            )
                .limit(limit, offset).toList().sortedByDefault().map { it.toDTO() }

            tasks.map {
                val reminders = reminderService.getByTaskID(
                    UUID.fromString(it.id)
                )
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

    fun completeTask(userId: Int, id: UUID, dateTime: String): Boolean {
        return transaction {
            val task = Task.findById(id) ?: return@transaction false
            if (task.user.id.value != userId) return@transaction false

            val doneDateTime = parseDateTime(dateTime)
            val updatedRow = Tasks.updateOperation(userId) {
                val updatedRows = update({ (Tasks.id eq id) }) {
                    it[this.doneDateTime] = doneDateTime
                }
                if (updatedRows > 0) id else null
            }

            if (updatedRow != null) {
                // Create a new task track entry
                TaskTrack.new {
                    this.task = task
                    this.doneDateTime = doneDateTime
                    this.status = Status.ACTIVE
                }
                true
            } else {
                false
            }
        }
    }

    fun unCompleteTask(userId: Int, id: UUID): Boolean {
        return transaction {
            val task = Task.findById(id) ?: return@transaction false
            if (task.user.id.value != userId) return@transaction false

            val updatedRow = Tasks.updateOperation(userId) {
                val updatedRows = update({ (Tasks.id eq id) }) {
                    it[doneDateTime] = null
                }
                if (updatedRows > 0) id else null
            }

            if (updatedRow != null) {
                TaskTrack.find { TaskTracks.taskId eq id }
                    .orderBy(TaskTracks.doneDateTime to SortOrder.DESC)
                    .firstOrNull()?.let { track ->
                        track.status = Status.INACTIVE
                    }
                true
            } else {
                false
            }
        }
    }

    fun getByIdAndUserId(id: UUID, userId: Int): TaskDTO? {
        return transaction {
            val task = Task.find { (Tasks.id eq id) and (Tasks.user eq userId) and (Tasks.status eq Status.ACTIVE) }
                .firstOrNull()?.toDTO()
            task?.let {
                val reminders = reminderService.getByTaskID(
                    UUID.fromString(it.id)
                )
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

    fun getTasksCompletedPerDayThisWeek(userId: Int): List<Int> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = today.minus((today.dayOfWeek.ordinal).toLong(), DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
        val completedPerDay = IntArray(7) { 0 }

        transaction {
            TaskTrack.find {
                (TaskTracks.status eq Status.ACTIVE) and
                        (TaskTracks.doneDateTime.date() greaterEq startOfWeek) and
                        (TaskTracks.doneDateTime.date() lessEq endOfWeek) and
                        (TaskTracks.taskId inSubQuery Tasks.slice(Tasks.id).select { Tasks.user eq userId })
            }.forEach { track ->
                val doneDate = track.doneDateTime.date
                val dayIdx = doneDate.dayOfWeek.ordinal // 0=Mon, 6=Sun
                completedPerDay[dayIdx]++
            }
        }
        return completedPerDay.toList()
    }

}