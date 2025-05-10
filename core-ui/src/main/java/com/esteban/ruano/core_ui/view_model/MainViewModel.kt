package com.esteban.ruano.core_ui.view_model

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core.interfaces.OperationListener
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.core_ui.view_model.state.MainEffect
import com.esteban.ruano.core_ui.view_model.state.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val operationListener: OperationListener
) : BaseViewModel<MainIntent, MainState, MainEffect>() {

    override fun createInitialState(): MainState {
        return MainState()
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