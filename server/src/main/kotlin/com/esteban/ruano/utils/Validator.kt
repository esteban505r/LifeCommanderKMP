package com.esteban.ruano.utils

object Validator {
    fun isValidTimeFormat(time: String): Boolean {
        val timeFormatRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$".toRegex()
        return timeFormatRegex.matches(time)
    }

    fun isValidDateFormat(date: String): Boolean {
        val dateFormatRegex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$".toRegex()
        return dateFormatRegex.matches(date)
    }

    fun isValidDateTimeFormat(dateTime: String): Boolean {
        val dateTimeFormatRegex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4} ([01]?[0-9]|2[0-3]):[0-5][0-9]$".toRegex()
        return dateTimeFormatRegex.matches(dateTime)
    }
}