package com.esteban.ruano.database.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object ExercisesWithWorkoutTracks : IntIdTable() {
    val exercise = reference("exercise", Exercises, ReferenceOption.CASCADE)
    val workoutTrack = reference("workoutTrack", WorkoutTracks, ReferenceOption.CASCADE)
}

class ExerciseWithWorkoutTrack(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ExerciseWithWorkoutTrack>(ExercisesWithWorkoutTracks)

    var exercise by Exercise referencedOn ExercisesWithWorkoutTracks.exercise
    var workoutDay by WorkoutTrack referencedOn ExercisesWithWorkoutTracks.workoutTrack
}