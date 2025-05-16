package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifecommander.models.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.TokenStorage
import services.habits.HabitService
import utils.DateUtils.parseDate
import utils.DateUtils.parseDateTime
import java.time.LocalDateTime

class HabitsViewModel(
    private val tokenStorage: TokenStorage,
    private val habitService: HabitService,
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(
        emptyList()
    )

    val habits = _habits

    val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun getHabits() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = habitService.getByDate(
                    token = tokenStorage.getToken() ?: "",
                    page = 0,
                    limit = 30,
                    date = LocalDateTime.now().toLocalDate().parseDate()
                )
                _habits.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun changeCheckHabit(id: String, checked: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (checked) {
                    habitService.completeHabit(
                        token = tokenStorage.getToken() ?: "",
                        id = id,
                        dateTime = LocalDateTime.now().parseDateTime()
                    )
                } else {
                    habitService.unCompleteHabit(
                        token = tokenStorage.getToken() ?: "",
                        id = id,
                        dateTime = LocalDateTime.now().parseDateTime()
                    )
                }
                _habits.value = _habits.value.map {
                    if (it.id == id) {
                        it.copy(done = checked)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun addHabit(name: String, note: String?, frequency: String, dateTime: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.addHabit(
                    token = tokenStorage.getToken() ?: "",
                    name = name,
                    note = note,
                    frequency = frequency,
                    dateTime = dateTime
                )
                getHabits()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateHabit(id: String, habit: Habit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.updateHabit(
                    token = tokenStorage.getToken() ?: "",
                    id = id,
                    habit = habit
                )
                getHabits()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.deleteHabit(
                    token = tokenStorage.getToken() ?: "",
                    id = id
                )
                getHabits()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }
}