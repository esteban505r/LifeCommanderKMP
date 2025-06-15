package com.esteban.ruano.service

import kotlinx.datetime.*
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Task
import com.esteban.ruano.database.entities.Tasks
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
                }
                id.let {
                    task.reminders?.forEach { reminder ->
                        if(reminder.id != null) reminderService.update(userId, UUID.fromString(reminder.id), UpdateReminderDTO(
                            id = reminder.id,
                            taskId = id.toString(),
                            time = reminder.time
                        ))
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
            val tasks = Task.find { (Tasks.user eq userId) and (Tasks.name.lowerCase() like "%${pattern.lowercase()}%") and (Tasks.status eq Status.ACTIVE) }
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
                .or ((Tasks.dueDateTime.date() lessEq startDate) and (Tasks.doneDateTime eq null)))
                .or (Tasks.scheduledDateTime.date() greaterEq startDate)
                .or ((Tasks.scheduledDateTime.date() lessEq startDate) and (Tasks.doneDateTime eq null))
            else (Tasks.dueDateTime.date() greaterEq startDate) or (Tasks.scheduledDateTime.date() greaterEq startDate)

        return transaction {
            val tasks = Task.find {
                (Tasks.user eq userId) and (Tasks.name.lowerCase() like "%${pattern.lowercase()}%")
                    // Check if dueDateTime falls after startDate
                    .and(query)
                    // Also if scheduledDateTime falls between startDate and endDate
                    .or(Tasks.scheduledDateTime.date() lessEq endDate)
                    // Check the state
                    .and (Tasks.status eq Status.ACTIVE)
                    // Check the done task should be hidden based on the doneTaskHiddenTimeInDays
                    .and((Tasks.doneDateTime eq null) or (Tasks.doneDateTime.date() greaterEq endDate.minus(doneTaskHiddenTimeInDays, DateTimeUnit.DAY)))

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
            val tasks = Task.find { (Tasks.user eq userId) and (Tasks.name.lowerCase() like "%${pattern.lowercase()}%") and (Tasks.dueDateTime eq null) and (Tasks.status eq Status.ACTIVE) }
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
            val updatedRow = Tasks.updateOperation(userId) {
                val updatedRows = update({ (Tasks.id eq id) }) {
                    it[doneDateTime] = parseDateTime(dateTime)
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun unCompleteTask(userId: Int, id: UUID): Boolean {
        return transaction {
            val updatedRow = Tasks.updateOperation(userId) {
                val updatedRows = update({ (Tasks.id eq id) }) {
                    it[doneDateTime] = null
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
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
            Task.find {
                (Tasks.user eq userId) and
                (Tasks.status eq Status.ACTIVE) and
                (Tasks.doneDateTime.isNotNull()) and
                (Tasks.doneDateTime.date() greaterEq startOfWeek) and
                (Tasks.doneDateTime.date() lessEq endOfWeek)
            }.forEach { task ->
                val doneDate = task.doneDate?.date
                if (doneDate != null) {
                    val dayIdx = doneDate.dayOfWeek.ordinal // 0=Mon, 6=Sun
                    completedPerDay[dayIdx]++
                }
            }
        }
        return completedPerDay.toList()
    }

}