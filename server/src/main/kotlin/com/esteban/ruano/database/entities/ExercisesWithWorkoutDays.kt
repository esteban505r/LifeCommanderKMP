package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object ExercisesWithWorkoutDays : UUIDTable() {
    val exercise = reference("exercise", Exercises, ReferenceOption.CASCADE)
    val workoutDay = reference("workoutDay", WorkoutDays, ReferenceOption.CASCADE)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
}

class ExerciseWithWorkoutDay(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<ExerciseWithWorkoutDay>(ExercisesWithWorkoutDays)

    var exercise by Exercise referencedOn ExercisesWithWorkoutDays.exercise
    var workoutDay by WorkoutDay referencedOn ExercisesWithWorkoutDays.workoutDay
    var user by User referencedOn ExercisesWithWorkoutDays.user
}