import com.esteban.ruano.models.habits.HabitDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import com.lifecommander.models.dashboard.HabitStats
import com.lifecommander.models.dashboard.TaskStats
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponseDTO(
    val nextTask: TaskDTO?,
    val nextHabit: HabitDTO?,
    val taskStats: TaskStatsDTO,
    val habitStats: HabitStatsDTO
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