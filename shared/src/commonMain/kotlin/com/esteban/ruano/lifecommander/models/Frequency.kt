package com.lifecommander.models

enum class Frequency(val value: String){
    ONE_TIME("one_time"),
    DAILY("daily"),
    WEEKLY("weekly"),

    BI_WEEKLY("bi_weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly");

    companion object {
        fun fromString(value: String): Frequency {
            return when (value) {
                "one_time" -> ONE_TIME
                "daily" -> DAILY
                "weekly" -> WEEKLY
                "monthly" -> MONTHLY
                "yearly" -> YEARLY
                else -> DAILY
            }
        }
    }
}