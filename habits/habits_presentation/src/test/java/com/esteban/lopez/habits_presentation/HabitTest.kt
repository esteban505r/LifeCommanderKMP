package com.esteban.ruano.habits_presentation

import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.formatTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseTime
import com.lifecommander.models.Habit
import com.esteban.ruano.habits_presentation.utilities.HabitsUtils.timeDoingIt
import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDateTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class HabitTest {
    @Test
    fun timeDoingItTest(){
        val habit = Habit("EXAMPLEID"
        , name = "EXAMPLENAME",
            type = "EXAMPLETYPE",
            done = true,
            dateTime = LocalDateTime.now()
                .minusHours(5).parseDateTime(),
            reminders = null,
            frequency = "EXAMPLEFREQUENCY",
            note = "EXAMPLENOTE"
        )
        assertEquals("05:00",
            habit.timeDoingIt()?.formatTime()?.substring(0,5))
    }
}