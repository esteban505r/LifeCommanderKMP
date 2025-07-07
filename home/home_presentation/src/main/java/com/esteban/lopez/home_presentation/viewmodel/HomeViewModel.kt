package com.esteban.ruano.home_presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            // This will be handled by the MainViewModel through the LocalMainIntent
            // The actual logout logic is now centralized in MainViewModel
        }
    }
} 