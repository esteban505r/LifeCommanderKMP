package com.esteban.ruano.timers_domain.use_case

data class TimerUseCases(
    val getTimerLists: GetTimerLists,
    val getTimerList: GetTimerList,
    val createTimerList: CreateTimerList,
    val updateTimerList: UpdateTimerList,
    val deleteTimerList: DeleteTimerList,
    val createTimer: CreateTimer,
    val updateTimer: UpdateTimer,
    val deleteTimer: DeleteTimer,
    val startTimer: StartTimer,
    val pauseTimer: PauseTimer,
    val resumeTimer: ResumeTimer,
    val stopTimer: StopTimer
)

