package com.esteban.ruano.lifecommander.models.timers

data class CompletedTimerInfo(
    val domainTimer: com.esteban.ruano.lifecommander.models.Timer,
    val listId: String,
    val userId: Int,
    val name: String
)