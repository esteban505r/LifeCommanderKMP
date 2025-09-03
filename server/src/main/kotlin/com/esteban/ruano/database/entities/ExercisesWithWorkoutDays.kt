package com.esteban.ruano.database.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
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