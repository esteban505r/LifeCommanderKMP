package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.MuscleGroup
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.workout.WorkoutDashboardDTO
import com.esteban.ruano.models.workout.WorkoutTrackDTO
import com.esteban.ruano.models.workout.day.UpdateWorkoutDayDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.utils.fromDateToLong
import com.esteban.ruano.utils.toDayOfWeek
import com.esteban.ruano.utils.toLocalTime
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

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

    fun getWorkoutDashboard(userId: Int): WorkoutDashboardDTO {
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

    fun getWorkoutDaysByDay(userId: Int, day: Int): List<WorkoutDayDTO> {
        if(day !in 1..7){
            throw Exception("Day must be between 1 and 7")
        }
        return transaction {
            val workoutDays = WorkoutDay.find((WorkoutDays.user eq userId) and (WorkoutDays.day eq day)).toList()
            if(workoutDays.isEmpty()){
                return@transaction emptyList<WorkoutDayDTO>()
            }
            val exercisesWithWorkoutDays = ExerciseWithWorkoutDay.find((ExercisesWithWorkoutDays.user eq userId)
                .and (ExercisesWithWorkoutDays.workoutDay inList workoutDays.map { it.id })).toList()

            val exerciseIds = exercisesWithWorkoutDays.map { it.exercise.id.value }
            val exercises = Exercise.find { Exercises.id inList exerciseIds }.toList()

            //val equipment = Equipment.find { Equipments.exercise inList exerciseIds }.groupBy { it.exercise.id.value }

            workoutDays.map {
                it.toDTO(
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

    // Workout Tracking Methods
    fun completeWorkout(userId: Int, workoutDayId: String, doneDateTime: String): Boolean {
        return transaction {
            // Verify the workout day belongs to the user
            val workoutDay = WorkoutDay.find { 
                (WorkoutDays.id eq UUID.fromString(workoutDayId)) and
                (WorkoutDays.user eq userId) and 
                (WorkoutDays.status eq Status.ACTIVE) 
            }.firstOrNull()
            
            if (workoutDay != null) {
                val id = WorkoutTracks.insertOperation(userId, doneDateTime.fromDateToLong()) {
                    insert {
                        it[WorkoutTracks.workoutDayId] = workoutDay.id
                        it[this.doneDateTime] = doneDateTime.toLocalDateTime()!!
                        it[status] = Status.ACTIVE
                    }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
                }
                id != null
            } else {
                false
            }
        }
    }

    fun unCompleteWorkout(userId: Int, trackId: String): Boolean = transaction {
        val trackUUID = UUID.fromString(trackId)

        // Subquery using modern DSL: select only id
        val activeWorkoutDays = WorkoutDays
            .select(WorkoutDays.id)
            .where { (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) }

        // Update active tracks linked to those workout days
        val updatedRows = WorkoutTracks.update({
            (WorkoutTracks.id eq trackUUID) and
                    (WorkoutTracks.status eq Status.ACTIVE) and
                    (WorkoutTracks.workoutDayId inSubQuery activeWorkoutDays)
        }) {
            it[status] = Status.INACTIVE
        }

        updatedRows > 0
    }



    fun getWorkoutsCompletedPerDayThisWeek(userId: Int): List<Int> {
        return transaction {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekStart = today.minus((today.dayOfWeek.ordinal).toLong(), DateTimeUnit.DAY)
            
            (0..6).map { i ->
                val day = weekStart.plus(i, DateTimeUnit.DAY)
                val start = day.atTime(0, 0)
                val end = day.atTime(23, 59)
                
                WorkoutTrack.find { 
                    (WorkoutTracks.doneDateTime greaterEq start) and 
                    (WorkoutTracks.doneDateTime lessEq end) and 
                    (WorkoutTracks.status eq Status.ACTIVE) and
                    (WorkoutTracks.workoutDayId inSubQuery WorkoutDays.slice(WorkoutDays.id).select { 
                        (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) 
                    })
                }.count().toInt()
            }
        }
    }

    fun getWorkoutTracksByDateRange(userId: Int, startDate: String, endDate: String): List<WorkoutTrackDTO> {
        return transaction {
            WorkoutTrack.find {
                (WorkoutTracks.doneDateTime greaterEq startDate.toLocalDateTime()!!) and
                (WorkoutTracks.doneDateTime lessEq endDate.toLocalDateTime()!!) and
                (WorkoutTracks.status eq Status.ACTIVE) and
                (WorkoutTracks.workoutDayId inSubQuery WorkoutDays.slice(WorkoutDays.id).select { 
                    (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) 
                })
            }.toList().map { it.toDTO() }
        }
    }

    fun deleteWorkoutTrack(userId: Int, trackId: String): Boolean {
        return transaction {
            val deletedRow = WorkoutTracks.deleteOperation(userId) {
                val updatedRows = WorkoutTracks.update({ 
                    (WorkoutTracks.id eq UUID.fromString(trackId)) and
                    (WorkoutTracks.workoutDayId inSubQuery WorkoutDays.slice(WorkoutDays.id).select { 
                        (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) 
                    })
                }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) UUID.fromString(trackId) else null
            }
            deletedRow != null
        }
    }

    /*fun getWorkoutDaysByDateRange(userId: Int, startDate: LocalDate, endDate: LocalDate): List<WorkoutDayDTO> {
        if (startDate > endDate) {
            throw Exception("Start date must be before end date")
        }
        return transaction {
            val workoutDays = WorkoutDay.find((WorkoutDays.user eq userId) and (WorkoutDays.date between startDate..endDate)).toList()
            if (workoutDays.isEmpty()) {
                return@transaction emptyList<WorkoutDayDTO>()
            }
            val exercisesWithWorkoutDays = ExerciseWithWorkoutDay.find((ExercisesWithWorkoutDays.user eq userId)
                .and (ExercisesWithWorkoutDays.workoutDay inList workoutDays.map { it.id })).toList()

            val exerciseIds = exercisesWithWorkoutDays.map { it.exercise.id.value }
            val exercises = Exercise.find { Exercises.id inList exerciseIds }.toList()

            //val equipment = Equipment.find { Equipments.exercise inList exerciseIds }.groupBy { it.exercise.id.value }

            workoutDays.map {
                it.toDTO(
                    exercises = exercises.map { e ->
                        e.toDTO(
                          //  equipmentDTO = equipment[e.id.value]?.map { it.toDTO() } ?: emptyList(),
                            equipmentDTO = emptyList()
                        )
                    }
                )
            }
        }
    }*/
}