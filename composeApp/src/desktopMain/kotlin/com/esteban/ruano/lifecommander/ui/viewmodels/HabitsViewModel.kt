package ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifecommander.models.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import services.auth.TokenStorageImpl
import services.habits.HabitService
import utils.DateUtils.parseDate
import utils.DateUtils.parseDateTime
import java.time.LocalDateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone

class HabitsViewModel(
    private val tokenStorageImpl: TokenStorageImpl,
    private val habitService: HabitService,
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(
        emptyList()
    )

    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _nextHabitTime = MutableStateFlow<Long?>(null)
    val nextHabitTime: StateFlow<Long?> = _nextHabitTime.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                updateNextHabitTime()
                delay(1000)
            }
        }
    }

    private fun updateNextHabitTime() {
        viewModelScope.launch {
            try {
                _nextHabitTime.value = habitService.getNextHabitOccurrence(
                    token = tokenStorageImpl.getToken() ?: "",
                    currentTime = LocalDateTime.now().parseDateTime()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getHabits() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = habitService.getByDate(
                    token = tokenStorageImpl.getToken() ?: "",
                    page = 0,
                    limit = 30,
                    date = LocalDateTime.now().toLocalDate().parseDate()
                )
                _habits.value = response
                updateNextHabitTime()
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
                        token = tokenStorageImpl.getToken() ?: "",
                        id = id,
                        dateTime = LocalDateTime.now().parseDateTime()
                    )
                } else {
                    habitService.unCompleteHabit(
                        token = tokenStorageImpl.getToken() ?: "",
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

    fun addHabit(name: String, note: String?, frequency: String, dateTime: String, reminders: List<com.esteban.ruano.lifecommander.models.HabitReminder>) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.addHabit(
                    token = tokenStorageImpl.getToken() ?: "",
                    name = name,
                    note = note,
                    frequency = frequency,
                    dateTime = dateTime,
                    reminders = reminders
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
                    token = tokenStorageImpl.getToken() ?: "",
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
                    token = tokenStorageImpl.getToken() ?: "",
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

    fun markHabitDone(habit: Habit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.run {
                    completeHabit(
                                token = tokenStorageImpl.getToken() ?: "",
                                id = habit.id,
                                dateTime = habit.dateTime ?: getCurrentDateTime(
                                    TimeZone.currentSystemDefault()
                                ).formatDefault()
                            )
                }
                getHabits()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}