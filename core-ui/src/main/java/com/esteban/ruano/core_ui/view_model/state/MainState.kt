package com.esteban.ruano.core_ui.view_model.state


import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.ViewState


data class MainState(
    val isSynced: Boolean = false,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
    ): ViewState

sealed class MainEffect: Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType = SnackbarType.INFO) : MainEffect()
    data object NavigateToLogin : MainEffect()
    data object NavigateToHome : MainEffect()
}

