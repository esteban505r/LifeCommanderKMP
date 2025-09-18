package com.esteban.ruano.service

import io.ktor.server.plugins.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.DatePeriod
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.converters.toHabitDTO
import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.entities.HabitTracks
import com.esteban.ruano.database.entities.Habits
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.habits.UpdateHabitDTO
import com.esteban.ruano.models.reminders.UpdateReminderDTO
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.HabitUtils
import com.esteban.ruano.utils.parseDate
import com.esteban.ruano.utils.parseDateTime
import com.lifecommander.models.Frequency
import kotlinx.datetime.plus
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.minute
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HabitService(
    val reminderService: ReminderService
) : BaseService() {

    data class HabitNotificationInfo(
        val incompleteHabits: List<Habit>,
        val totalHabits: Int
    )

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
                    .orderBy(Habits.baseDateTime.minute() to SortOrder.ASC).limit(limit).offset(offset*limit).toList()

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
                .orderBy(Habits.baseDateTime.minute() to SortOrder.ASC).limit(limit).offset(offset*limit).toList()
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
            // Get all habits created before or on the end date (similar to fetchAll with date)
            val baseQuery = if (excludeDaily) {
                (Habits.user eq userId) and 
                (Habits.name.lowerCase() like "%${pattern.lowercase()}%") and 
                (Habits.baseDateTime.date() lessEq endDate) and
                (Habits.status eq Status.ACTIVE) and
                (Habits.frequency neq Frequency.DAILY.value)
            } else {
                (Habits.user eq userId) and 
                (Habits.name.lowerCase() like "%${pattern.lowercase()}%") and 
                (Habits.baseDateTime.date() lessEq endDate) and
                (Habits.status eq Status.ACTIVE)
            }

            val habits = Habit.find(baseQuery)
                .orderBy(Habits.baseDateTime.minute() to SortOrder.ASC)
                .limit(limit).offset(offset*limit)
                .toList()

            val result = mutableListOf<HabitDTO>()
            
            // For each habit, check if it's relevant for any day in the date range
            for (habit in habits) {
                var isRelevantForRange = false
                
                // Check each day in the range to see if this habit is relevant
                var currentDate = startDate
                while (currentDate <= endDate) {
                    val tracking = HabitTrack.find {
                        (HabitTracks.habitId eq habit.id) and (HabitTracks.status eq Status.ACTIVE)
                    }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

                    val dto = habit.toHabitDTO()
                    val isDone = HabitUtils.isDone(dto, tracking, currentDate)
                    
                    // If the habit is not done for this date, it's relevant for the range
                    if (!isDone) {
                        isRelevantForRange = true
                        break
                    }
                    
                    currentDate = currentDate.plus(DatePeriod(days = 1))
                }
                
                // Only include habits that are relevant for the date range
                if (isRelevantForRange) {
                    // Use the start date to determine the completion status for the result
                    val tracking = HabitTrack.find {
                        (HabitTracks.habitId eq habit.id) and (HabitTracks.status eq Status.ACTIVE)
                    }.orderBy(HabitTracks.doneDateTime to SortOrder.DESC).firstOrNull()

                    val dto = habit.toHabitDTO()
                    val isDone = HabitUtils.isDone(dto, tracking, startDate)
                    result.add(dto.copy(done = isDone))
                }
            }

            result.map {
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

            val isDone = HabitUtils.isDone(habit.toHabitDTO(), tracking, doneDateTime.toLocalDateTime().date)

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

            val isDone = HabitUtils.isDone(habit.toHabitDTO(), tracking, unDoneDate.toLocalDateTime().date)
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

    fun getDueHabitsForNotification(userId: Int, today: LocalDate): HabitNotificationInfo {
        return transaction {
            // Get all active habits for this user that are due today
            val dueHabits = Habit.find {
                (Habits.status eq Status.ACTIVE) and
                (Habits.user eq userId) and
                (Habits.baseDateTime.date() lessEq today)
            }.toList()

            val incompleteHabits = mutableListOf<Habit>()
            for (habit in dueHabits) {
                // Check if habit is already done for today
                val isDoneToday = HabitTrack.find {
                    (HabitTracks.habitId eq habit.id) and
                    (HabitTracks.status eq Status.ACTIVE) and
                    (HabitTracks.doneDateTime.date() eq today)
                }.count() > 0

                if (!isDoneToday) {
                    incompleteHabits.add(habit)
                }
            }

            HabitNotificationInfo(incompleteHabits, dueHabits.size)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun shouldSendHabitNotification(
        userId: Int,
        lastNotificationTime: Long,
        notificationFrequencyMinutes: Int
    ): Boolean {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val timeSinceLastNotification = currentTimeMillis - lastNotificationTime
        val notificationIntervalMs = notificationFrequencyMinutes * 60 * 1000L
        
        return timeSinceLastNotification >= notificationIntervalMs
    }

}