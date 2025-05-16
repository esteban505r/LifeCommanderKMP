package com.esteban.ruano.service

import io.ktor.server.plugins.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.converters.toHabitDTO
import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.entities.HabitTracks
import com.esteban.ruano.database.entities.Habits
import com.esteban.ruano.database.models.Frequency
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.habits.UpdateHabitDTO
import com.esteban.ruano.models.reminders.UpdateReminderDTO
import com.esteban.ruano.utils.HabitUtils
import com.esteban.ruano.utils.parseDate
import com.esteban.ruano.utils.parseDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.minute
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class HabitService(
    val reminderService: ReminderService
) : BaseService() {

    fun create(userId: Int, habit: CreateHabitDTO): UUID? {
        return transaction {
            val insertedRow = Habits.insertOperation(userId) {
             val item = insert {
                    it[name] = habit.name
                    it[frequency] = habit.frequency
                    it[note] = habit.note
                    it[baseDateTime] = parseDateTime(habit.dateTime!!)
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)
                UUID.fromString(item.toString())
            }
            if (insertedRow != null) {
                habit.reminders.forEach {
                    reminderService.create(userId, it.copy(
                        habitId = insertedRow.toString()
                    ))
                }
            }
            insertedRow
        }
    }

    fun update(userId: Int, id:UUID,habit: UpdateHabitDTO): Boolean {
        return transaction {
            val updatedRow = Habits.updateOperation(userId) {
                val updatedRows = update({ (Habits.id eq id) }) { row ->
                    habit.name?.let { row[name] = it }
                    habit.note?.let { row[note] = it }
                    habit.dateTime?.let { row[baseDateTime] = parseDateTime(it) }
                    habit.frequency?.let { row[frequency] = it }
                }
                if (updatedRows > 0) id else null
            }
            if(updatedRow != null){
                val currentReminders = reminderService.getByHabitID(id)
                val remindersToDelete =currentReminders.filter {
                    habit.reminders?.firstOrNull { reminder -> reminder.id == it.id.toString() } == null
                }
                remindersToDelete.forEach {
                    reminderService.delete(userId, it.id.value)
                }
                habit.reminders?.forEach {
                    if(it.id!=null) {
                        reminderService.update(userId, UUID.fromString(it.id), UpdateReminderDTO(
                            id = it.id,
                            habitId = id.toString(),
                            time = it.time,
                        ))
                    }
                    else{
                        reminderService.create(userId, it.copy(
                            habitId = id.toString()
                        ))
                    }
                }
            }
            updatedRow != null
        }
    }

    fun delete(userId: Int,id: UUID): Boolean {
        return transaction {
            val deletedRow = Habits.deleteOperation(userId) {
                val updatedRows = Habits.update({ (Habits.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }
            deletedRow != null
        }
    }


    fun fetchAll(userId: Int, pattern: String, limit: Int, offset: Long, date: LocalDate): List<HabitDTO> {
        return transaction {
            val habits =
                Habit.find { (Habits.user eq userId) and (Habits.name.lowerCase() like "%${pattern.lowercase()}%") and (Habits.baseDateTime.date() lessEq date) and (Habits.status eq Status.ACTIVE) }
                    .orderBy(Habits.baseDateTime.minute() to SortOrder.ASC).limit(limit, offset).toList()

            val result = mutableListOf<HabitDTO>()
            for (habit in habits) {
                val tracking = HabitTrack.find {
                    (HabitTracks.habitId eq habit.id) and (HabitTracks.status eq Status.ACTIVE)
                }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

                val dto = habit.toHabitDTO()
                HabitUtils.isDone(dto, tracking, date).let {
                    result.add(dto.copy(done = it))
                }

            }
            result.map {
                val reminders = reminderService.getByHabitID(UUID.fromString(it.id))
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

    fun fetchAll(userId: Int, pattern: String, limit: Int, offset: Long): List<HabitDTO> {
        return transaction {
            val habits = Habit.find { (Habits.user eq userId) and (Habits.name.lowerCase() like "%${pattern.lowercase()}%") and (Habits.frequency neq Frequency.ONE_TIME.value and (Habits.status eq Status.ACTIVE)) }
                .orderBy(Habits.baseDateTime.minute() to SortOrder.ASC).limit(limit, offset).toList()
                .map { it.toHabitDTO() }
            habits.map {
                val reminders = reminderService.getByHabitID(UUID.fromString(it.id))
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
        excludeDaily: Boolean = false
    ): List<HabitDTO> {
        return transaction {
            val query = if (excludeDaily) {
                (Habits.user eq userId) and 
                (Habits.name.lowerCase() like "%${pattern.lowercase()}%") and 
                (Habits.baseDateTime.date() greaterEq startDate) and
                (Habits.baseDateTime.date() lessEq endDate) and
                (Habits.status eq Status.ACTIVE) and
                (Habits.frequency neq Frequency.DAILY.value)
            } else {
                (Habits.user eq userId) and 
                (Habits.name.lowerCase() like "%${pattern.lowercase()}%") and 
                (Habits.baseDateTime.date() greaterEq startDate) and
                (Habits.baseDateTime.date() lessEq endDate) and
                (Habits.status eq Status.ACTIVE)
            }

            val habits = Habit.find(query)
                .orderBy(Habits.baseDateTime.minute() to SortOrder.ASC)
                .limit(limit, offset)
                .toList()
                .map { it.toHabitDTO() }

            habits.map {
                val reminders = reminderService.getByHabitID(UUID.fromString(it.id))
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }


    fun completeHabit(id: String, doneDateTime: String, userId: Int): Boolean {
        return transaction {
            val tracking = HabitTrack.find {
                (HabitTracks.habitId eq UUID.fromString(id)) and (HabitTracks.status eq Status.ACTIVE)
            }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

            val habit = Habit.findById(UUID.fromString(id)) ?: throw BadRequestException("Habit not found")

            val isDone = HabitUtils.isDone(habit.toHabitDTO(), tracking, parseDateTime(doneDateTime).date)

            if (!isDone) {
                val insertedRow = HabitTracks.insertOperation(
                    userId
                ) {
                    insert {
                        it[habitId] = habit.id
                        it[this.doneDateTime] = parseDateTime(doneDateTime)
                        it[status] = Status.ACTIVE
                    }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value.toString()
                    UUID.fromString(habit.id.toString())
                }
                insertedRow != null
            } else {
                false
            }
        }
    }

    fun unCompleteHabit(id: String, unDoneDate: String,userId: Int): Boolean {
        return transaction {
            val tracking = HabitTrack.find {
                (HabitTracks.habitId eq UUID.fromString(id)) and (HabitTracks.status eq Status.ACTIVE)
            }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

            val habit = Habit.findById(UUID.fromString(id)) ?: throw BadRequestException("Habit not found")

            val isDone = HabitUtils.isDone(habit.toHabitDTO(), tracking, parseDateTime(unDoneDate).date)
            if (isDone) {
                val updatedRow = HabitTracks.updateOperation(
                    userId){
                    val updatedRows = HabitTracks.update({ HabitTracks.habitId eq UUID.fromString(id) }) { row ->
                        row[status] = Status.INACTIVE
                    }
                    if (updatedRows > 0) UUID.fromString(id) else null
                }
                updatedRow != null
            } else {
                false
            }
        }
    }

    fun getByIdAndUserId(id: UUID, userId: Int, date: String): HabitDTO? {
        return transaction {
            val habit =
                Habit.find { (Habits.user eq userId) and (Habits.id eq id) and (Habits.status eq Status.ACTIVE) }
                    .firstOrNull()?.toHabitDTO()
            if (habit == null) {
                return@transaction null
            }
            val tracking = HabitTrack.find {
                (HabitTracks.habitId eq UUID.fromString(habit.id)) and (HabitTracks.status eq Status.ACTIVE)
            }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

            val isDone = HabitUtils.isDone(habit, tracking, parseDate(date))

            val result = if (isDone) {
                habit.copy(done = true)
            } else {
                habit.copy(done = false)
            }
            result.let {
                val reminders = reminderService.getByHabitID(UUID.fromString(it.id))
                it.copy(reminders = reminders.map { it.toDTO() })
            }
        }
    }

}