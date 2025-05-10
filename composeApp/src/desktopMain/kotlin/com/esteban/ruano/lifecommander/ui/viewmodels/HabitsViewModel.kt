package ui.viewmodels


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import services.auth.TokenStorage
import services.habits.HabitService
import services.habits.models.Frequency
import services.habits.models.HabitResponse
import utils.DateUtils.parseDate
import utils.DateUtils.parseDateTime
import utils.DateUtils.toLocalDateTime
import utils.StatusBarService
import utils.TimeBasedItemUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HabitsViewModel(
    private val habitService: HabitService,
    private val tokenStorage: TokenStorage,
    private val statusBarService: StatusBarService
) : ViewModel(){

    private val _habits = MutableStateFlow<List<HabitResponse>>(
        emptyList()
    )

    val habits = _habits

    private val _loading = MutableStateFlow(false)

    val loading = _loading.asStateFlow()

    fun getHabits(){
        viewModelScope.launch {
            getHabitsSuspend()
        }
    }

    private suspend fun getHabitsSuspend(){
        _loading.value = true
        try{
            val response = habitService.getByDate(
                token = tokenStorage.getToken() ?: "",
                page = 0,
                limit = 30,
                date = LocalDate.now().parseDate(),
            )
            _habits.value = response
            statusBarService.updateHabitStatus(
                TimeBasedItemUtils.getHabitStatusBarText(
                    habits = response,
                    currentTime = LocalDateTime.now()
                )
            )
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        finally {
            _loading.value = false
        }
    }


    fun changeCheckHabit(id:String,checked: Boolean) {
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
                getHabitsSuspend()
            }
            catch (e: Exception) {
               e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun addHabit(name: String, note: String?, frequency: Frequency, dateTime: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.addHabit(
                    token = tokenStorage.getToken() ?: "",
                    name = name,
                    note = note,
                    frequency = frequency.value,
                    dateTime = dateTime
                )
                getHabitsSuspend()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun updateHabit(id: String, habit: HabitResponse) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.updateHabit(
                    token = tokenStorage.getToken() ?: "",
                    id = id,
                    habit = habit
                )
                getHabitsSuspend()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun deleteHabit(it: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                habitService.deleteHabit(
                    token = tokenStorage.getToken() ?: "",
                    id = it
                )
                getHabitsSuspend()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _loading.value = false
            }
        }
    }

}