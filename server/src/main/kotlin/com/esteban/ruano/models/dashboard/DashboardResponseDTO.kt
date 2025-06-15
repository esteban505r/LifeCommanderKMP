import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.nutrition.RecipeDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import com.lifecommander.models.dashboard.HabitStats
import com.lifecommander.models.dashboard.JournalEntryDTO
import com.lifecommander.models.dashboard.TaskStats
import com.lifecommander.models.dashboard.TransactionDTO
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponseDTO(
    val nextTask: TaskDTO?,
    val nextHabit: HabitDTO?,
    val taskStats: TaskStatsDTO,
    val habitStats: HabitStatsDTO,
    // Finance
    val recentTransactions: List<TransactionDTO> = emptyList(),
    val accountBalance: Double = 0.0,
    // Meals
    val todayCalories: Int = 0,
    val mealsLogged: Int = 0,
    val nextMeal: RecipeDTO? = null,
    // Workout
    val todayWorkout: WorkoutDayDTO? = null,
    val caloriesBurned: Int = 0,
    val workoutStreak: Int = 0,
    // Journal
    val journalCompleted: Boolean = false,
    val journalStreak: Int = 0,
    val recentJournalEntries: List<JournalEntryDTO> = emptyList(),
    // Weekly/Monthly Progress
    val weeklyTaskCompletion: Float = 0f,
    val weeklyHabitCompletion: Float = 0f,
    val weeklyWorkoutCompletion: Float = 0f,
    val weeklyMealLogging: Float = 0f,
    // Daily completion data for charts
    val tasksCompletedPerDayThisWeek: List<Int> = emptyList(),
    val habitsCompletedPerDayThisWeek: List<Int> = emptyList(),
    val workoutsCompletedPerDayThisWeek: List<Int> = emptyList(),
    val mealsLoggedPerDayThisWeek: List<Int> = emptyList()
)

@Serializable
data class TaskStatsDTO(
    val total: Int,
    val completed: Int,
    val highPriority: Int
)

@Serializable
data class HabitStatsDTO(
    val total: Int,
    val completed: Int,
    val currentStreak: Int
)
