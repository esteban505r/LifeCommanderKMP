package com.esteban.ruano.utils

import com.esteban.ruano.database.entities.Exercises
import com.esteban.ruano.database.entities.Habits
import com.esteban.ruano.database.entities.Tasks
import com.esteban.ruano.database.models.MuscleGroup
import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.repository.HabitRepository
import com.esteban.ruano.repository.TaskRepository
import com.esteban.ruano.repository.WorkoutRepository
import com.esteban.ruano.service.ReminderService
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

// FirstRunSeeder.kt

class FirstRunSeeder(
    private val habitRepo: HabitRepository,
    private val taskRepo: TaskRepository,
    private val exerciseRepo: WorkoutRepository,
) {

    /**
     * Idempotent: checks if user already has content before inserting.
     * Safe to call multiple times.
     */
    @OptIn(ExperimentalTime::class)
    fun seedForNewUser(userId: Int) {
        // choose consistent base times
        val now = getCurrentDateTime(TimeZone.currentSystemDefault())

        // --- 1) Seed Habit: Drink water ---
        val habitId = habitRepo.create(
            userId = userId,
            habit = CreateHabitDTO(
                name = "Drink water",
                frequency = "DAILY",
                note = "8 cups a day",
                dateTime = now.formatDefault(),
                reminders = listOf()
            )
        )

        // --- 2) Seed Task: Plan my week ---
        taskRepo.create(
            userId = userId,
            task = CreateTaskDTO(
                name = "Plan my week (15m)",
                note = "Write top 3 priorities",
                priority = 0,
                scheduledDateTime = now.toInstant(TimeZone.currentSystemDefault()).plus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).formatDefault(),
                dueDateTime = now.toInstant(TimeZone.currentSystemDefault()).plus(2.days).toLocalDateTime(TimeZone.currentSystemDefault()).formatDefault(),
                reminders = listOf()
            )
        )

        // --- 3) Seed Exercise (example for your workout stack) ---
        exerciseRepo.createExercise(
            userId = userId,
            exercise = ExerciseDTO(
                id = "",
                name = "5-min Mobility",
                description = "Neck rolls, shoulder circles, hip openers",
                restSecs = 20,
                baseReps = 8,
                baseSets = 1,
                muscleGroup = MuscleGroup.CORE.value
            )
        )

    }

}
