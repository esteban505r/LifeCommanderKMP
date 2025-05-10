package ui.services.dailyjournal.models

data class CreatePomodoroRequest(
    val startDateTime: String,
    val endDateTime: String,
) 