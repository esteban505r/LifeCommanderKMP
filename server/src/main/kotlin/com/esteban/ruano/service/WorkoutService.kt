package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.MuscleGroup
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.workout.WorkoutDashboardDTO
import com.esteban.ruano.models.workout.WorkoutTrackDTO
import com.esteban.ruano.models.workout.ExerciseTrackDTO
import com.esteban.ruano.models.workout.day.UpdateWorkoutDayDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.toLocalTime
import com.esteban.ruano.utils.fromDateToLong
import com.esteban.ruano.utils.toDayOfWeek
import io.ktor.server.plugins.BadRequestException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.datetime.date
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
                    day = workoutDayId,
                    time = "00:00",
                    name = "Free day",
                    exercises = emptyList(),
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
            val exercises = Exercise.find { Exercises.user eq userId and (Exercises.name.lowerCase() like "%${filter.lowercase()}%") }.limit(limit).offset(offset).toList()
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

    fun getWorkoutDaysByDay(userId: Int, day: Int, dateTime: String? = null): List<WorkoutDayDTO> {
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

            //Get which exercises are completed for the day
            val exercisesTracks = dateTime?.let {
                getCompletedExercisesForDay(
                    userId = userId,
                    workoutDayId = workoutDays.firstOrNull()?.id?.value.toString(),
                    dateTime = it
                )
            } ?: emptyList()

            val workoutCompleted = if(dateTime != null){
                WorkoutTrack.find{
                    (WorkoutTracks.workoutDayId inList workoutDays.map { it.id }) and
                            (WorkoutTracks.status eq Status.ACTIVE)  and
                            (WorkoutTracks.doneDateTime.date() eq dateTime.toLocalDateTime().date)
                }.count() > 0
            } else {
                false
            }


            workoutDays.map {
                it.toDTO(
                    exercises = exercises.map { e ->
                        e.toDTO(
                          //  equipmentDTO = equipment[e.id.value]?.map { it.toDTO() } ?: emptyList(),
                            equipmentDTO = emptyList(),
                        ).copy(
                            isCompleted = exercisesTracks.contains(e.id.value.toString())
                        )
                    }
                ).copy(
                    isCompleted = workoutCompleted,
                )
            }
        }
    }

    // Workout Tracking Methods
    fun completeWorkout(userId: Int, dayId: Int, doneDateTime: String): Boolean {
        return transaction {
            // Verify the workout day belongs to the user
            val workoutDay = WorkoutDay.find {
                (WorkoutDays.day eq dayId) and
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



    @OptIn(ExperimentalTime::class)
    fun getWorkoutsCompletedPerDayThisWeek(userId: Int): List<Int> = transaction {
        val today = Clock.System.now().toLocalDateTimeKt(TimeZone.currentSystemDefault()).date
        val weekStart = today.minus(today.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY)

        // Subquery of the user's active workout days (reused below)
        val userWorkoutDays = WorkoutDays
            .select(WorkoutDays.id)
            .where { (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) }

        (0..6).map { i ->
            val day = weekStart.plus(i, DateTimeUnit.DAY)

            WorkoutTrack.find {
                (WorkoutTracks.doneDateTime.date() eq day) and
                        (WorkoutTracks.status eq Status.ACTIVE) and
                        (WorkoutTracks.workoutDayId inSubQuery userWorkoutDays)
            }.count().toInt()
        }
    }

    fun getWorkoutTracksByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<WorkoutTrackDTO> = transaction {
        val userWorkoutDays = WorkoutDays
            .select(WorkoutDays.id)
            .where { (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) }

        WorkoutTrack.find {
            (WorkoutTracks.doneDateTime.date() greaterEq startDate.toLocalDate()) and
                    (WorkoutTracks.doneDateTime.date() lessEq endDate.toLocalDate()) and
                    (WorkoutTracks.status eq Status.ACTIVE) and
                    (WorkoutTracks.workoutDayId inSubQuery userWorkoutDays)
        }.toList().map { it.toDTO() }
    }

    fun deleteWorkoutTrack(userId: Int, trackId: String): Boolean = transaction {
        val userWorkoutDays = WorkoutDays
            .select(WorkoutDays.id)
            .where { (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) }

        val deletedRow = WorkoutTracks.deleteOperation(userId) {
            val updatedRows = WorkoutTracks.update({
                (WorkoutTracks.id eq UUID.fromString(trackId)) and
                        (WorkoutTracks.workoutDayId inSubQuery userWorkoutDays)
            }) {
                it[status] = Status.DELETED
            }
            if (updatedRows > 0) UUID.fromString(trackId) else null
        }
        deletedRow != null
    }


    fun bindExerciseToDay(userId: Int, exerciseId: String, workoutDayDay: Int): Boolean {
        return transaction {
            var workoutDay = WorkoutDay.find { (WorkoutDays.user eq userId) and (WorkoutDays.day eq workoutDayDay) }
                .firstOrNull()
            if (workoutDay == null) {
                // Auto-create the WorkoutDay if it doesn't exist
                val workoutDayUUID = createWorkoutDay(userId, workoutDayDay)
                workoutDay = workoutDayUUID?.let { WorkoutDay.findById(it) }
            }
            val exercise = Exercise.findById(UUID.fromString(exerciseId))
            if (workoutDay != null && exercise != null) {
                val existing = ExerciseWithWorkoutDay.find {
                    (ExercisesWithWorkoutDays.user eq userId) and
                            (ExercisesWithWorkoutDays.exercise eq exercise.id) and
                            (ExercisesWithWorkoutDays.workoutDay eq workoutDay.id)
                }.firstOrNull()
                if (existing == null) {
                    ExerciseWithWorkoutDay.new {
                        this.user = User[userId]
                        this.exercise = exercise
                        this.workoutDay = workoutDay
                    }
                    true
                } else {
                    false // Already bound
                }
            } else {
                false // WorkoutDay or Exercise not found
            }
        }
    }

    fun unbindExerciseFromDay(userId: Int, exerciseId: String, workoutDayDay: Int): Boolean {
        return transaction {
            val workoutDay = WorkoutDay.find { (WorkoutDays.user eq userId) and (WorkoutDays.day eq workoutDayDay) }.firstOrNull()
            val exercise = Exercise.findById(UUID.fromString(exerciseId))
            if (workoutDay != null && exercise != null) {
                val binding = ExerciseWithWorkoutDay.find {
                    (ExercisesWithWorkoutDays.user eq userId) and
                    (ExercisesWithWorkoutDays.exercise eq exercise.id) and
                    (ExercisesWithWorkoutDays.workoutDay eq workoutDay.id)
                }.firstOrNull()
                binding?.let {
                    it.delete()
                    true
                } ?: false
            } else {
                false // WorkoutDay or Exercise not found
            }
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

    // Exercise Tracking Methods
    fun completeExercise(userId: Int, exerciseId: String, workoutDayId: String, doneDateTime: String): Boolean {
        return transaction {
            // Verify the exercise and workout day belong to the user
            val exercise = Exercise.find { 
                (Exercises.id eq UUID.fromString(exerciseId)) and
                (Exercises.user eq userId) and 
                (Exercises.status eq Status.ACTIVE) 
            }.firstOrNull()
            
            val workoutDay = WorkoutDay.find { 
                (WorkoutDays.id eq UUID.fromString(workoutDayId)) and
                (WorkoutDays.user eq userId) and 
                (WorkoutDays.status eq Status.ACTIVE) 
            }.firstOrNull()
            
            if (exercise != null && workoutDay != null) {
                val existingTrack = ExerciseTrack.find {
                    (ExerciseTracks.exerciseId eq exercise.id) and
                    (ExerciseTracks.workoutDayId eq workoutDay.id) and
                    (ExerciseTracks.doneDateTime.date() eq doneDateTime.toLocalDateTime().date) and
                    (ExerciseTracks.status eq Status.ACTIVE)
                }.firstOrNull()

                if (existingTrack != null) {
                    throw BadRequestException("Exercise already completed for this workout day.")
                }

                val id = ExerciseTracks.insertOperation(userId, doneDateTime.fromDateToLong()) {
                    insert {
                        it[ExerciseTracks.exerciseId] = exercise.id
                        it[ExerciseTracks.workoutDayId] = workoutDay.id
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

    fun unCompleteExercise(userId: Int, trackId: String): Boolean = transaction {
        val trackUUID = UUID.fromString(trackId)

        // Subquery using modern DSL: select only id
        val activeWorkoutDays = WorkoutDays
            .select(WorkoutDays.id)
            .where { (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) }

        val activeExercises = Exercises
            .select(Exercises.id)
            .where { (Exercises.user eq userId) and (Exercises.status eq Status.ACTIVE) }

        // Update active tracks linked to those workout days and exercises
        val updatedRows = ExerciseTracks.update({
            (ExerciseTracks.id eq trackUUID) and
                    (ExerciseTracks.status eq Status.ACTIVE) and
                    (ExerciseTracks.workoutDayId inSubQuery activeWorkoutDays) and
                    (ExerciseTracks.exerciseId inSubQuery activeExercises)
        }) {
            it[status] = Status.INACTIVE
        }

        updatedRows > 0
    }
    fun getExerciseTracksByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<ExerciseTrackDTO> = transaction {
        // Subqueries without slice()
        val userWorkoutDays = WorkoutDays
            .select(WorkoutDays.id)
            .where { (WorkoutDays.user eq userId) and (WorkoutDays.status eq Status.ACTIVE) }

        val userExercises = Exercises
            .select(Exercises.id)
            .where { (Exercises.user eq userId) and (Exercises.status eq Status.ACTIVE) }

        ExerciseTrack.find {
            (ExerciseTracks.doneDateTime.date() greaterEq startDate.toLocalDate()) and
                    (ExerciseTracks.doneDateTime.date() lessEq endDate.toLocalDate()) and
                    (ExerciseTracks.status eq Status.ACTIVE) and
                    (ExerciseTracks.workoutDayId inSubQuery userWorkoutDays) and
                    (ExerciseTracks.exerciseId inSubQuery userExercises)
        }.toList().map { it.toDTO() }
    }

    fun getCompletedExercisesForDay(
        userId: Int,
        workoutDayId: String,
        dateTime: String
    ): List<String> = transaction {
        val userExercises = Exercises
            .select(Exercises.id)
            .where { (Exercises.user eq userId) and (Exercises.status eq Status.ACTIVE) }

        ExerciseTrack.find {
            (ExerciseTracks.workoutDayId eq UUID.fromString(workoutDayId)) and
                    (ExerciseTracks.status eq Status.ACTIVE) and
                    (ExerciseTracks.exerciseId inSubQuery userExercises) and
                    (ExerciseTracks.doneDateTime.date() eq dateTime.toLocalDateTime().date)
        }.map { it.exercise.id.value.toString() }
    }

}