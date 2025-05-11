package com.esteban.ruano.database.models

enum class Frequency(val value: String){
    ONE_TIME("one_time"),
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly")
}