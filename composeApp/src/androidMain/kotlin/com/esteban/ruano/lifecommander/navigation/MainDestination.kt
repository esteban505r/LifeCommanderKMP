package com.esteban.ruano.lifecommander.navigation


import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.composables.ObserveAsEvents
import com.esteban.ruano.core_ui.utils.CustomSnackBarVisuals
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.view_model.MainViewModel
import com.esteban.ruano.core_ui.view_model.state.MainEffect
import com.esteban.ruano.lifecommander.screens.MainScreen
import kotlinx.coroutines.launch

@Composable
fun MainDestination(
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel = hiltViewModel()
) {

    val state = viewModel.viewState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    /**
     * Handles navigation based on [MainEffect].
     *
     * @param navEvent The navigation event to handle.
     */
    fun handleNavigation(navEvent: MainEffect) {
        when (navEvent) {
            is MainEffect.ShowSnackBar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        CustomSnackBarVisuals.fromType(
                            navEvent.type,
                            navEvent.message
                        )
                    )
                    keyboardController?.hide()
                }
            }
            MainEffect.NavigateToLogin -> {
                // This will be handled by the MainScreen based on authentication state
            }
            MainEffect.NavigateToHome -> {
                // This will be handled by the MainScreen based on authentication state
            }
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        viewModel.effect.collect { handleNavigation(it) }
    }

    ObserveAsEvents(flow = SnackbarController.events, snackbarHostState) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(it.customSnackBarVisuals.toCustomSnackBarVisuals(context))
            if(result == SnackbarResult.ActionPerformed){
                it.action?.action?.invoke()
            }
        }
    }
    MainScreen(
        isLogged = state.value.isAuthenticated,
        snackbarHostState = snackbarHostState,
    )
}