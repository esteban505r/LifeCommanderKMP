package com.esteban.ruano.service

import com.esteban.ruano.database.entities.Reminder
import com.esteban.ruano.database.entities.Reminders
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.reminders.CreateReminderDTO
import com.esteban.ruano.models.reminders.UpdateReminderDTO
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID


class ReminderService: BaseService() {

    fun create(
        userId: Int,
        createReminderDTO: CreateReminderDTO,
    ): UUID? {
        return transaction {
            val id = Reminders.insertOperation(
                userId
            ) {
                insert {
                    it[enabled] = true
                    it[time] = createReminderDTO.time
                    it[taskId] = createReminderDTO.taskId?.let { UUID.fromString(it) }
                    it[habitId] = createReminderDTO.habitId?.let { UUID.fromString(it) }
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id
        }
    }

    fun update(
        userId: Int,
        id: UUID,
        dto: UpdateReminderDTO,
    ): Boolean {
        return transaction {
            val updatedRow = Reminders.updateOperation(userId) {
                val updatedRows = update({ (Reminders.id eq id) }) { row ->
                    dto.taskId?.let { row[taskId] = UUID.fromString(it) }
                    dto.habitId?.let { row[habitId] = UUID.fromString(it) }
                    dto.time.let { row[time] = it.toLong() }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = Reminders.deleteOperation(userId) {
                val updatedRows = Reminders.update({ (Reminders.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getByTaskID(taskId: UUID): List<Reminder> {
        return transaction {
            Reminder.find{
                (Reminders.taskId eq taskId) and (Reminders.status eq Status.ACTIVE)
            }
        }.toList()
    }

    fun getByHabitID(habitId: UUID): List<Reminder> {
        return transaction {
            Reminder.find{
                (Reminders.habitId eq habitId) and (Reminders.status eq Status.ACTIVE)
            }
        }.toList()
    }

    fun getDueReminders(currentTime: Long): List<Reminder> {
        return transaction {
            Reminder.find {
                (Reminders.enabled eq true) and
                (Reminders.status eq Status.ACTIVE) and
                (Reminders.time lessEq currentTime)
            }.toList()
        }
    }
}