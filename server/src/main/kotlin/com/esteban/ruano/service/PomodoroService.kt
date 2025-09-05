package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Pomodoro
import com.esteban.ruano.database.entities.Pomodoros
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.pomodoros.CreatePomodoroDTO
import com.esteban.ruano.models.pomodoros.PomodoroDTO
import com.esteban.ruano.models.pomodoros.UpdatePomodoroDTO
import com.esteban.ruano.utils.parseDateTime
import com.esteban.ruano.utils.parseDateTimeWithSeconds
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

class PomodoroService : BaseService() {

    fun create(userId: Int, pomodoro: CreatePomodoroDTO): UUID? {
        return transaction {
            val id = Pomodoros.insertOperation(userId) {
                insert {
                    it[startDateTime] = parseDateTimeWithSeconds(pomodoro.startDateTime)
                    it[endDateTime] = parseDateTimeWithSeconds(pomodoro.endDateTime)
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
            id
        }
    }

    fun update(userId: Int, id: UUID, pomodoro: UpdatePomodoroDTO): Boolean {
        return transaction {
            val updatedRow = Pomodoros.updateOperation(userId) {
                val updatedRows = update({ (Pomodoros.id eq id) }) { row ->
                    pomodoro.startDateTime?.let { row[startDateTime] = parseDateTime(it) }
                    pomodoro.endDateTime?.let { row[endDateTime] = parseDateTime(it) }
                }
                if (updatedRows > 0) id else null
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = Pomodoros.deleteOperation(userId) {
                val updatedRows = Pomodoros.update({ (Pomodoros.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }

    fun getByUserId(userId: Int, limit: Int, offset: Long): List<PomodoroDTO> {
        return transaction {
            Pomodoro.find {
                (Pomodoros.user eq userId) and (Pomodoros.status eq Status.ACTIVE)
            }.limit(limit).offset(offset).toList().map { it.toDTO() }
        }
    }

    fun getByDateRange(
        userId: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        offset: Long
    ): List<PomodoroDTO> {
        return transaction {
            Pomodoro.find {
                (Pomodoros.user eq userId) and
                (Pomodoros.status eq Status.ACTIVE) and
                (Pomodoros.startDateTime.date() greaterEq startDate) and
                (Pomodoros.startDateTime.date() lessEq endDate)
            }.limit(limit).offset(offset).toList().map { it.toDTO() }
        }
    }

    fun getByIdAndUserId(id: UUID, userId: Int): PomodoroDTO? {
        return transaction {
            Pomodoro.find {
                (Pomodoros.id eq id) and (Pomodoros.user eq userId) and (Pomodoros.status eq Status.ACTIVE)
            }.firstOrNull()?.toDTO()
        }
    }
} 