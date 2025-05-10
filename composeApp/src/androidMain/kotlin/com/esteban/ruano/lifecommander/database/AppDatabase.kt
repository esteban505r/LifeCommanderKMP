package com.esteban.ruano.lifecommander.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.esteban.ruano.core_data.local.HistoryTrackDao
import com.esteban.ruano.core_data.local.model.HistoryTrack
import com.esteban.ruano.habits_data.local.HabitsDao
import com.esteban.ruano.habits_data.local.model.Habit
import com.esteban.ruano.tasks_data.local.TaskDao
import com.esteban.ruano.tasks_data.local.model.Task
import com.esteban.ruano.workout_data.local.WorkoutDao
import com.esteban.ruano.workout_data.local.model.Equipment
import com.esteban.ruano.workout_data.local.model.Exercise
import com.esteban.ruano.workout_data.local.model.ExercisesWithEquipments
import com.esteban.ruano.workout_data.local.model.ExercisesWithWorkoutDays
import com.esteban.ruano.workout_data.local.model.Resource
import com.esteban.ruano.workout_data.local.model.WorkoutDay

@Database(
    entities = [
        HistoryTrack::class,
        Task::class, Habit::class, Resource::class,
        WorkoutDay::class, Equipment::class,
        Exercise::class,
        ExercisesWithWorkoutDays::class,
        ExercisesWithEquipments::class], version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitsDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun historyTrackDao(): HistoryTrackDao
}