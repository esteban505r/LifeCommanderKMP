package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Equipment
import com.esteban.ruano.database.entities.Exercise
import com.esteban.ruano.database.entities.WorkoutDay
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.esteban.ruano.models.workout.equiment.EquipmentDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.utils.formatTime


fun WorkoutDay.toDTO(exercises: List<ExerciseDTO>): WorkoutDayDTO {
    return WorkoutDayDTO(
        id = this.id.toString(),
        day = this.day,
        time = formatTime(this.time),
        name = this.name,
        exercises = exercises,
    )
}

fun Exercise.toDTO(
    equipmentDTO: List<EquipmentDTO> = listOf()
): ExerciseDTO {
    return ExerciseDTO(
        id = this.id.toString(),
        name = this.name,
        description = this.description,
        restSecs = this.restSecs,
        baseSets = this.baseSets,
        baseReps = this.baseReps,
        muscleGroup = this.muscleGroup.value,
        equipment = equipmentDTO,
        resource = this.resource?.toDTO()
    )
}

fun Equipment.toDTO(): EquipmentDTO {
    return EquipmentDTO(
        id = this.id.value,
        name = this.name,
        description = this.description,
        resource = this.resource.toDTO()
    )
}


