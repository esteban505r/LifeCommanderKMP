package com.esteban.ruano.core_ui.utils

import androidx.compose.runtime.compositionLocalOf
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.core_ui.view_model.state.MainState

val LocalMainIntent = compositionLocalOf<(MainIntent) -> Unit> {
    error("No MainIntentFunction provided")
}

val LocalMainState = compositionLocalOf<MainState> {
    error("No MainState provided")
}
