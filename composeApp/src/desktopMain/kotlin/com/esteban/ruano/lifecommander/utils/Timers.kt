package utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import models.TimerModel

object Timers {
    private val _timers = MutableStateFlow(listOf<TimerModel>())
    val timers get() = _timers.asStateFlow()

    fun add(timer: TimerModel) {
        val newList = _timers.value.toMutableList()
        newList.add(timer)
        _timers.value = newList
    }

    fun addAll(timers: List<TimerModel>) {
        val newList = _timers.value.toMutableList()
        newList.addAll(timers)
        _timers.value = newList
    }

    fun remove(timer: TimerModel) {
        val newList = _timers.value.toMutableList()
        newList.remove(timer)
        _timers.value = newList
    }

    fun replaceTimers(timers: List<TimerModel>) {
        _timers.value = timers
    }

    suspend fun saveTimers(dataStore: DataStore<Preferences>) {
        dataStore.edit {
            it[timersKey] = Gson().toJson(_timers.value)
        }
    }
}