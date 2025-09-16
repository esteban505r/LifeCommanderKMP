package com.esteban.ruano.workout_data.repository
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.CreateExerciseTrack
import com.esteban.ruano.workout_data.datasources.WorkoutDataSource
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.repository.WorkoutRepository
import com.esteban.ruano.workout_domain.model.Workout
import kotlinx.coroutines.flow.first

class WorkoutRepositoryImpl (
    private val remoteDataSource: WorkoutDataSource,
    private val localDataSource: WorkoutDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
): BaseRepository(),WorkoutRepository {
    override suspend fun getWorkoutDays(): Result<List<Workout>> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getWorkoutDays() },
            remoteFetch = { remoteDataSource.getWorkoutDays() },
            forceRefresh = false,
        )
    }

    override suspend fun getWorkoutDayById(workoutId: Int): Result<Workout> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getWorkoutDayById(workoutId.toString()) },
            remoteFetch = { remoteDataSource.getWorkoutDayById(workoutId.toString()) },
            forceRefresh = false,
        )
    }

    override suspend fun getWorkoutDayStatus(workoutDayId: String,dateTime:String): Result<List<ExerciseDayStatus>> {
        return doRemoteRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            remoteFetch = { remoteDataSource.getWorkoutDayStatus(workoutDayId,dateTime) },
        )
    }

    override suspend fun getWorkoutDayByNumber(number: Int): Result<Workout> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getWorkoutDayByNumber(number) },
            remoteFetch = { remoteDataSource.getWorkoutDayByNumber(number) },
            forceRefresh = false,
        )
    }

    override suspend fun getExercisesByWorkoutDay(workoutDayId:Int): Result<List<Exercise>> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getExercisesByWorkoutDay(workoutDayId.toString()) },
            remoteFetch = { remoteDataSource.getExercisesByWorkoutDay(workoutDayId.toString()) },
            forceRefresh = false,
        )
    }

    override suspend fun getExerciseById(exerciseId: String): Result<Exercise> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getExerciseById(exerciseId) },
            remoteFetch = { remoteDataSource.getExerciseById(exerciseId) },
            forceRefresh = false,
        )
    }

    override suspend fun undoExercise(trackId: String): Result<Unit> {
        return doRemoteRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            remoteFetch = {
                remoteDataSource.undoExercise(
                    trackId
                )
            }
        )
    }

    override suspend fun getExercises(): Result<List<Exercise>> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getExercises() },
            remoteFetch = { remoteDataSource.getExercises() },
            forceRefresh = false,
        )
    }

    override suspend fun getWorkoutDaysWithExercises(): Result<List<Workout>> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getWorkoutDaysWithExercises() },
            remoteFetch = { remoteDataSource.getWorkoutDays() },
            forceRefresh = false,
        )
    }

    override suspend fun saveExercise(exercise: Exercise): Result<Unit> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.saveExercise(exercise) },
            remoteFetch = { remoteDataSource.saveExercise(exercise) },
            forceRefresh = false,
        )
    }

    override suspend fun saveWorkoutDay(workout: Workout): Result<Unit> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.saveWorkoutDay(workout) },
            remoteFetch = { remoteDataSource.saveWorkoutDay(workout) },
            forceRefresh = false,
        )
    }

    override suspend fun updateWorkoutDay(id:String, workout: Workout): Result<Unit> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.updateWorkoutDay(id,workout) },
            remoteFetch = { remoteDataSource.updateWorkoutDay(id,workout) },
            forceRefresh = false,
        )
    }

    override suspend fun linkExerciseWithWorkoutDay(workoutDayId: Int, exerciseId: String): Result<Unit> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.linkExerciseWithWorkoutDay(workoutDayId.toString(),
                exerciseId) },
            remoteFetch = { remoteDataSource.linkExerciseWithWorkoutDay(workoutDayId.toString(), exerciseId.toString()) },
            forceRefresh = false,
        )
    }

    override suspend fun unlinkExerciseWithWorkoutDay(workoutDayId: Int, exerciseId: String): Result<Unit> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.unLinkExerciseWithWorkoutDay(workoutDayId.toString(),
                exerciseId
            ) },
            remoteFetch = { remoteDataSource.unLinkExerciseWithWorkoutDay(workoutDayId.toString(),
                exerciseId
            ) },
            forceRefresh = false,
        )
    }

    override suspend fun getWorkoutDashboard(): Result<WorkoutDashboard> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = { localDataSource.getWorkoutDashboard() },
            remoteFetch = { remoteDataSource.getWorkoutDashboard() },
            forceRefresh = false,
        )
    }

    override suspend fun addSet(dto: CreateExerciseSetTrackDTO) : Result<Unit> {
        return doRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            lastFetchTime = preferences.loadLastFetchTime().first(),
            localFetch = {  },
            remoteFetch = { remoteDataSource.addSet(dto) },
            forceRefresh = false,
        )
    }

    override suspend fun removeSet(id: String): Result<Unit> {
        return doRemoteRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            remoteFetch = {
                remoteDataSource.removeSet(id)
            }
        )
    }

    override suspend fun completeExercise(track: CreateExerciseTrack): Result<Unit> {
        return doRemoteRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            remoteFetch = {
                remoteDataSource.completeExercise(track)
            }
        )
    }

    override suspend fun updateExercise(
        id: String,
        exercise: Exercise
    ): Result<Unit> {
        return doRemoteRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            remoteFetch = {
                remoteDataSource.updateExercise(id,exercise)
            }
        )
    }

    override suspend fun deleteExercise(id: String): Result<Unit> {
        return doRemoteRequest(
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            remoteFetch = {
                remoteDataSource.deleteExercise(id)
            }
        )
    }


}