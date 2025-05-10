package com.esteban.ruano.core_ui.utils

import com.esteban.ruano.core_ui.R


//Commented reminders are not supported yet by backend
sealed class ReminderType(val time: Long) {
    data object FifteenMinutes : ReminderType(900000)
    data object OneHour : ReminderType(3600000)
    data object EightHours : ReminderType(28800000)
//    data object OneDay : ReminderType(86400000)
//    data object OneWeek : ReminderType(604800000)
//    data object OneMonth : ReminderType(2592000000)
//    data class Custom(val customTime: Long) : ReminderType(customTime)

    companion object{
        fun ReminderType.toResource(): Int{
            return when(this){
                is FifteenMinutes -> R.string.fifteen_minutes
                is OneHour -> R.string.one_hour
                is EightHours -> R.string.eight_hours
//                is OneDay -> R.string.one_day
//                is OneWeek -> R.string.one_week
//                is OneMonth -> R.string.one_month
//                is Custom -> R.string.custom_time
            }
        }
        fun Long.toReminderType(): ReminderType{
            return when(this){
                900000L -> FifteenMinutes
                3600000L -> OneHour
                28800000L -> EightHours
//                86400000L -> OneDay
//                604800000L -> OneWeek
//                2592000000L -> OneMonth
//                else -> Custom(this)
                else ->  FifteenMinutes
            }
        }
    }
}

