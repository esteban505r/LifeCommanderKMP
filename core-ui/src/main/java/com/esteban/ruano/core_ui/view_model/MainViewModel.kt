package com.esteban.ruano.core_ui.view_model

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.interfaces.OperationListener
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.core_ui.view_model.state.MainEffect
import com.esteban.ruano.core_ui.view_model.state.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val operationListener: OperationListener,
    private val preferences: Preferences
) : BaseViewModel<MainIntent, MainState, MainEffect>() {

    override fun createInitialState(): MainState {
        return MainState()
    }

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            val authToken = preferences.loadAuthToken().first()
            val isAuthenticated = authToken.isNotEmpty()
            emitState { currentState.copy(isAuthenticated = isAuthenticated) }
            
            if (isAuthenticated) {
                sendEffect { MainEffect.NavigateToHome }
            } else {
                sendEffect { MainEffect.NavigateToLogin }
            }
        }
    }

    override fun handleIntent(intent: MainIntent) {
        intent.let {
            when (it) {
                MainIntent.Sync -> {
                    viewModelScope.launch {
                        sync()
                    }
                }

                is MainIntent.ShowSnackBar -> {
                    sendEffect {
                        MainEffect.ShowSnackBar(it.message,it.type)
                    }
                }
                
                MainIntent.Logout -> {
                    viewModelScope.launch {
                        preferences.clearAuthToken()
                        emitState { currentState.copy(isAuthenticated = false) }
                        sendEffect { MainEffect.NavigateToLogin }
                    }
                }
                
                MainIntent.CheckAuthentication -> {
                    checkAuthenticationStatus()
                }
            }
        }
    }

    private suspend fun sync(){
        emitState { currentState.copy(isLoading = true) }
        val synced = operationListener.onOperation()
        if(synced) {
            emitState { MainState(isSynced = true) }
        }else{
            emitState { MainState(isError = true, errorMessage = "Error syncing") }
        }
    }




}