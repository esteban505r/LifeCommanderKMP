package com.esteban.ruano.service

import WorkoutDashboardDTO
import kotlinx.datetime.DayOfWeek
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.MuscleGroup
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.workout.day.UpdateWorkoutDayDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import toDayOfWeek
import toLocalTime
import java.util.UUID

class WorkoutService : BaseService() {

    fun getWorkoutDaysWithExercises(userId: Int): List<WorkoutDayDTO> {
        return transaction {
            val exercisesWithWorkoutDays = ExerciseWithWorkoutDay.find(ExercisesWithWorkoutDays.user eq userId).toList()

            val exerciseIds = exercisesWithWorkoutDays.map { it.exercise.id.value }
            val exercises = Exercise.find { Exercises.id inList exerciseIds }.toList()

            //val equipment = Equipment.find { Equipments.exercise inList exerciseIds }.groupBy { it.exercise.id.value }

            exercisesWithWorkoutDays.map {
                val workoutDay = it.workoutDay
                workoutDay.toDTO(
                    exercises = exercises.map { e ->
                        e.toDTO(
                          //  equipmentDTO = equipment[e.id.value]?.map { it.toDTO() } ?: emptyList(),
                            equipmentDTO = emptyList()
                        )
                    }
                )
            }
        }
    }

    fun getWorkoutDashboard(userId: Int): WorkoutDashboardDTO{
        return transaction {
            val workoutDays = getWorkoutDaysWithExercises(userId)
            val totalExercises = Exercises.select((Exercises.user eq userId) and (Exercises.status eq Status.ACTIVE)).count()
            WorkoutDashboardDTO(
                workoutDays = workoutDays,
                totalExercises = totalExercises
            )
        }
    }

    fun getWorkoutDayById(userId: Int,workoutDayId:Int):WorkoutDayDTO{
            if(workoutDayId == 0){
                    throw Exception("No workout day found")
            }
            return transaction {
            val workoutDay = WorkoutDay.find(((WorkoutDays.user eq userId) and (WorkoutDays.day eq workoutDayId))).firstOrNull()
            if(workoutDay == null){
                println("No workout day found for user $userId and workout day $workoutDayId")
                return@transaction WorkoutDayDTO(
                    id = "-1",
                    name = "Free day",
                    exercises = emptyList(),
                    day = workoutDayId,
                    time = "00:00"
                )
            }
            else{
                println("Workout day found for user $userId and workout day $workoutDayId")
            }
            val exercisesWithWorkoutDays = ExerciseWithWorkoutDay.find((ExercisesWithWorkoutDays.user eq userId)
            .and (ExercisesWithWorkoutDays.workoutDay eq workoutDay!!.id)).toList()

            /*if (exercisesWithWorkoutDays.isEmpty()) {
                throw Exception("No workout days found for user $userId and workout day $workoutDayId")
            }*/

            val exerciseIds = exercisesWithWorkoutDays.map { it.exercise.id.value }
            val exercises = Exercise.find { Exercises.id inList exerciseIds }.toList()

            //val equipment = Equipment.find { Equipments.exercise inList exerciseIds }.groupBy { it.exercise.id.value }

            workoutDay.toDTO(
                exercises = exercises.map { e ->
                    e.toDTO(
                      //  equipmentDTO = equipment[e.id.value]?.map { it.toDTO() } ?: emptyList(),
                        equipmentDTO = emptyList()
                    )
                }
            )
        }
    }

    fun getExercises(userId: Int, filter: String, limit: Int, offset: Long):List<ExerciseDTO> {
        return transaction {
            val exercises = Exercise.find { Exercises.user eq userId and (Exercises.name.lowerCase() like "%${filter.lowercase()}%") }.limit(limit, offset).toList()
            val exerciseWithEquipment = exercises.map { e ->
                //val equipment = Equipment.find { Equipments.exercise eq e.id }.toList()
                e.toDTO(
                  //  equipmentDTO = equipment.filter { it.exercise.id.value == e.id.value }.map { it.toDTO() }
                    equipmentDTO = emptyList()
                )
            }
            exerciseWithEquipment
        }
    }

    fun createExercise(userId: Int, exercise: ExerciseDTO): UUID? {
        return transaction {
            Exercises.insertOperation(userId) {
               insert {
                    it[name] = exercise.name
                    it[description] = exercise.description
                    it[restSecs] = exercise.restSecs
                    it[baseReps] = exercise.baseReps
                    it[baseSets] = exercise.baseSets
                    it[muscleGroup] = try{MuscleGroup.valueOf(exercise.muscleGroup.uppercase())}catch (e:Exception){throw Exception("Muscle group not found")}
                    it[user] = userId
               }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }

        }
    }

    private fun createWorkoutDay(userId: Int, day: Int): UUID? {
        if(day !in 1..7){
            throw Exception("Day must be between 1 and 7")
        }
        return transaction {
            WorkoutDays.insertOperation(userId) {
                insert {
                    it[name] = day.toDayOfWeek().name
                    it[this.day] = day
                    it[time] = "00:00".toLocalTime()
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(WorkoutDays.id)?.value
                    ?: throw Exception("Workout day not created")
            }
        }
    }

    fun updateWorkoutDay(userId: Int, dayId: String, workoutDay: UpdateWorkoutDayDTO):Boolean {
        val workoutDayId = try{
            val result = getWorkoutDayById(userId,dayId.toInt())
            if(result.id == "-1") {
                createWorkoutDay(userId, dayId.toInt()).toString()
            }
            else{
                result.id
            }
        }
        catch (e:Exception){
            throw Exception("Workout day not found")
        }

        println("Workout day id: $workoutDayId")

        return transaction {
            val updatedRow = WorkoutDays.updateOperation(userId) {
                val updatedRows = update({ (WorkoutDays.id eq UUID.fromString(workoutDayId)) }) {
                    workoutDay.name?.let { name -> it[WorkoutDays.name] = name }
                    workoutDay.day?.let { day -> it[WorkoutDays.day] = day }
                    workoutDay.time?.let { time -> it[WorkoutDays.time] = time.toLocalTime() }
                }
                if (updatedRows > 0) UUID.fromString(workoutDayId) else null
            }
            updatedRow?.let {
                val exercises = workoutDay.exercises?.map { it.id } ?: emptyList()
                val exercisesWithWorkoutDays = ExerciseWithWorkoutDay.find((ExercisesWithWorkoutDays.user eq userId)
                    .and (ExercisesWithWorkoutDays.workoutDay eq UUID.fromString(workoutDayId))).toList()
                val exercisesToDelete = exercisesWithWorkoutDays.filter {
                    exercises.firstOrNull { exercise -> exercise == it.exercise.id.value.toString() } == null
                }
                val exercisesToInsert = workoutDay.exercises?.filter {
                    exercisesWithWorkoutDays.firstOrNull { eww -> eww.exercise.id.value.toString() == it.id } == null
                }
                exercisesToInsert?.forEach { exercise ->
                    ExerciseWithWorkoutDay.new {
                        this.exercise = Exercise[UUID.fromString(exercise.id)]
                        this.workoutDay = WorkoutDay[UUID.fromString(workoutDayId)]
                        this.user = User[userId]
                    }
                }
                exercisesToDelete.forEach {
                    it.delete()
                }
            }
            updatedRow != null
        }
    }

}