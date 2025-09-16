package com.esteban.ruano.core_ui.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.utils.CustomSnackbarVisualsWithUiText
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.utils.SnackbarEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


/**
 * This is the interface for the view state.
 */
interface ViewState

/**
 * This is the interface for the user intent.
 */
interface UserIntent

/**
 * This is the interface for the navigation effect.
 */
interface Effect

/**
 * This is the Base view model implementing the MVI (Model-View-Intent) architecture.
 * @param Intent: The user intent that triggers changes in the view model.
 * @param State: The view state representing the current state of the UI.
 * @param Effect: The navigation effect representing UI-related side effects.
 *
 * @author Esteban Lopez
 */
abstract class BaseViewModel<Intent : UserIntent, State : ViewState, Effect : com.esteban.ruano.core_ui.view_model.Effect> : ViewModel() {


    // Channel to handle navigation effects.
    private val _effect: Channel<Effect> = Channel()
    val effect
        get() = _effect.receiveAsFlow()

    private val initialState: State by lazy { createInitialState() }

    abstract fun createInitialState(): State

    // MutableStateFlow to hold the current view state.
    private val _viewState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val viewState
        get() = _viewState.asStateFlow()

    val currentState: State
        get() = viewState.value

    private val _intent: MutableSharedFlow<Intent> = MutableSharedFlow()
    val intent
        get() = _intent.asSharedFlow()

    init {
        // Start listening to the user intents.
        subscribeIntent()
    }

    /**
     * Start listening to the user intent flow.
     */
    private fun subscribeIntent() {
        viewModelScope.launch {
            intent.collect {
                handleIntent(it)
            }
        }
    }

    /**
     * Handle each user intent.
     * @param intent: The user's intent that triggers changes in the view model.
     */
    abstract fun handleIntent(intent: Intent)

    /**
     * Set a new user intent to trigger a change in the view model.
     * @param intent: The user's intent.
     */
    fun performAction(intent: Intent) {
        val newUserIntent = intent
        viewModelScope.launch { _intent.emit(newUserIntent) }
    }

    /**
     * Emit a new view state by applying a reduction function to the current state.
     * @param reduce: The reduction function that defines how to derive the new state.
     */
    protected fun emitState(reduce: State.() -> State) {
        val newState = currentState.reduce()
        _viewState.value = newState
    }

    /**
     * Set a new navigation effect to trigger UI-related side effects.
     * @param builder: A lambda function that constructs the navigation effect.
     */
    protected fun sendEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }


    /**
     * Send an error effect to display a global error snackbar.
     * @param message: The error message to display.
     */
    protected fun sendErrorEffect(message: UiText? = null) {
        viewModelScope.launch {
            SnackbarController.sendEvent(
                SnackbarEvent(
                    CustomSnackbarVisualsWithUiText.fromType(
                        SnackbarType.ERROR,
                        message = message?: UiText.StringResource(com.esteban.ruano.core_ui.R.string.error_unknown)
                    )
                )
            )
        }
    }

    /**
     * Send a success effect to display a global success snackbar.
     * @param message: The success message to display.
     */
    protected fun sendSuccessEffect(message: UiText? = null) {
        viewModelScope.launch {
            SnackbarController.sendEvent(
                SnackbarEvent(
                    CustomSnackbarVisualsWithUiText.fromType(
                        SnackbarType.SUCCESS,
                        message = message
                            ?: UiText.StringResource(com.esteban.ruano.core_ui.R.string.success)
                    )
                )
            )
        }
    }
}