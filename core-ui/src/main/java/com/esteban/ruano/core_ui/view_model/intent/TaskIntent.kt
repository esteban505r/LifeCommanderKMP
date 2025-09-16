package com.esteban.ruano.core_ui.view_model.intent

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class MainIntent : UserIntent {
    data object Sync : MainIntent()
    data class ShowSnackBar(
        val message: String,
        val type: SnackbarType = SnackbarType.INFO
    ) : MainIntent()
    data object Logout : MainIntent()
    data object CheckAuthentication : MainIntent()
}