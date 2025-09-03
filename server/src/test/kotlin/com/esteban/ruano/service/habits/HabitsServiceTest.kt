package com.esteban.ruano.service.habits

import com.esteban.ruano.database.entities.Habit
import com.esteban.ruano.database.entities.HabitTrack
import com.esteban.ruano.database.entities.HabitTracks
import com.esteban.ruano.database.entities.Habits
import com.esteban.ruano.database.entities.HistoryTracks
import com.esteban.ruano.database.entities.Reminders
import com.esteban.ruano.database.entities.Tasks
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.habits.UpdateHabitDTO
import com.esteban.ruano.models.reminders.CreateReminderDTO
import com.esteban.ruano.service.HabitService
import com.esteban.ruano.service.ReminderService
import com.esteban.ruano.utils.DateUtils
import com.lifecommander.models.Frequency
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HabitsServiceTest {

    private lateinit var habitService : HabitService
    private lateinit var reminderService : ReminderService


    private val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())

    @Before
    fun setup() {
        //Ensure you have a postgres database running on localhost:5431 with the name testdb
        habitService = HabitService(ReminderService())
        reminderService = ReminderService()
        Database.Companion.connect(
            url = "jdbc:postgresql://localhost:5431/testdb",
            driver = "org.postgresql.Driver",
            user = "testuser",
            password = "testpassword"
        )
        TransactionManager.Companion.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())
        val timeZone = TimeZone.Companion.currentSystemDefault()
        val yesterdayInstant = Clock.System.now() - 1L.toDuration(DurationUnit.DAYS)
        val yesterdayDateTime = yesterdayInstant.toLocalDateTime(timeZone)
        val dayBeforeYesterdayInstant = Clock.System.now() - 2L.toDuration(DurationUnit.DAYS)
        val dayBeforeYesterdayTime = dayBeforeYesterdayInstant.toLocalDateTime(timeZone)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                Tasks,
                Reminders,
                Habits,
                HabitTracks,
                HistoryTracks
            )
        }

        transaction {
            HabitTracks.deleteAll()
            Reminders.deleteAll()
            Habits.deleteAll()
            Users.deleteAll()
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
            Habits.insert {
                it[name] = "Daily Exercise"
                it[note] = "Workout for 30 mins"
                it[baseDateTime] = currentDate
                it[user] = 1
            }
            Habits.insert {
                it[name] = "Read a Book"
                it[note] = "Read at least 10 pages"
                it[baseDateTime] = currentDate
                it[user] = 1
            }
            Habits.insert {
                it[name] = "Meditation"
                it[note] = "10 minutes meditation"
                it[baseDateTime] = currentDate
                it[user] = 1
            }
            Habits.insert {
                it[name] = "Hydration Reminder"
                it[note] = "Drink 8 glasses of water"
                it[baseDateTime] = currentDate
                it[user] = 1
            }
        }

        transaction {
            val exerciseId = Habit.Companion.find { Habits.name eq "Daily Exercise" }.single().id
            val readBookId = Habit.Companion.find { Habits.name eq "Read a Book" }.single().id

            HabitTracks.insert {
                it[habitId] = exerciseId
                it[doneDateTime] = yesterdayDateTime
                it[status] = Status.ACTIVE
            }
            HabitTracks.insert {
                it[habitId] = readBookId
                it[doneDateTime] = dayBeforeYesterdayTime
                it[status] = Status.ACTIVE
            }
        }


    }

    @Test
    fun `should fetch habits for user`() {
        val habits = habitService.fetchAll(1, "", 30, 0, currentDateTime.date)
        assert(habits.isNotEmpty())
        assert(habits.any { it.name == "Daily Exercise" })
    }

    @Test
    fun `should return empty for non-existent user`() {
        val habits = habitService.fetchAll(2, "", 30, 0, currentDateTime.date)
        assert(habits.isEmpty())
    }

    @Test
    fun `should filter out completed habit if hidden time is 0 days`() {
        val habits = habitService.fetchAll(
            1,
            "",
            30,
            0,
            currentDateTime.date
        )
        assert(habits.size == 4)
        assert(habits.any { it.name == "Daily Exercise" && !it.done })
    }

    @Test
    fun `should mark habit as complete`() {
        val habit = habitService.fetchAll(1, "", 30, 0, currentDateTime.date).first { it.name == "Read a Book" }

        val result = habitService.completeHabit(habit.id,
            DateUtils.formatDateTime(currentDateTime),
            1)

        assert(result)

        val tracks = transaction {
            HabitTrack.Companion.find { HabitTracks.habitId eq UUID.fromString(habit.id) }.toList()
        }
        assert(tracks.isNotEmpty())
        assert(tracks.any { it.status == Status.ACTIVE })
    }

    @Test
    fun `should uncomplete habit`() {
        val habit = habitService.fetchAll(1, "", 30, 0, currentDateTime.date).first { it.name == "Daily Exercise" }

        habitService.unCompleteHabit(habit.id, DateUtils.formatDateTime(currentDateTime), 1)

        val habitUnDone = habitService.getByIdAndUserId(
            UUID.fromString(habit.id), 1,
            DateUtils.formatDate(currentDateTime.date)
        )

        assert(habitUnDone?.done == false)
    }

    @Test
    fun `should create habit with reminders`() {
        val dto = CreateHabitDTO(
            name = "New Habit",
            note = "Test habit",
            dateTime = DateUtils.formatDateTime(currentDateTime),
            frequency = Frequency.DAILY.value,
            reminders = listOf(
                CreateReminderDTO(
                    id = null,
                    time = 3600
                ),
                CreateReminderDTO(
                    id = null,
                    time = 64800
                )
            )
        )
        val habitId = habitService.create(1, dto)

        assert(habitId != null)

        val reminders = transaction { reminderService.getByHabitID(habitId!!) }
        assert(reminders.size == 2)
    }

    @Test
    fun `should update habit name and reminders`() {
        val habit = habitService.fetchAll(1, "", 30, 0, currentDateTime.date).first { it.name == "Read a Book" }

        val updatedDto = UpdateHabitDTO(
            name = "Updated Habit",
            dateTime = DateUtils.formatDateTime(currentDateTime),
            frequency = Frequency.DAILY.value,
            reminders = listOf(
                CreateReminderDTO(
                    id = null,
                    time = 32400
                ),
                CreateReminderDTO(
                    id = null,
                    time = 64800
                )
            )
        )

        val updated = habitService.update(1, UUID.fromString(habit.id), updatedDto)
        assert(updated)

        val updatedHabit = habitService.getByIdAndUserId(
            UUID.fromString(habit.id), 1,
            DateUtils.formatDate(currentDateTime.date)
        )
        assert(updatedHabit?.name == "Updated Habit")

        val reminders = transaction {
            reminderService.getByHabitID(UUID.fromString(habit.id))
        }
        assert(reminders.size == 2)
        assert(reminders.any {  it.time == 32400.toLong() })
        assert(reminders.any { it.time == 64800.toLong() })
    }

    @Test
    fun `should soft delete habit`() {
        val habit = habitService.fetchAll(1, "", 30, 0, currentDateTime.date).first { it.name == "Read a Book" }

        val result = habitService.delete(1, UUID.fromString(habit.id))

        assert(result)

        val deletedHabit = habitService.getByIdAndUserId(
            UUID.fromString(habit.id), 1,
            DateUtils.formatDate(currentDateTime.date)
        )
        assert(deletedHabit == null)
    }

}