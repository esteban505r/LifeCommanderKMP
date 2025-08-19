package com.esteban.ruano.lifecommander.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.esteban.ruano.core_ui.utils.CustomSnackBarVisuals
import com.esteban.lopez.navigation.screens.LoggedScreen
import com.esteban.lopez.onboarding_presentation.auth.navigation.LoginDestination
import com.esteban.ruano.core_ui.view_model.state.MainState

@Composable
fun MainScreen(
    snackbarHostState: SnackbarHostState,
    state: MainState ,
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(
            WindowInsets.systemBars.asPaddingValues()
        ),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, snackbar = { snackbarData: SnackbarData ->
                val customVisuals = snackbarData.visuals as? CustomSnackBarVisuals
                if (customVisuals != null) {
                    Snackbar(
                        snackbarData = snackbarData,
                        contentColor = customVisuals.contentColor,
                        containerColor = customVisuals.containerColor
                    )
                } else {
                    Snackbar(snackbarData = snackbarData)
                }
            })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ){
            if(state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else if(state.isAuthenticated) {
                LoggedScreen(
                    onRootNavigate = {
                        navController.navigate(it)
                    }
                )
            }
            else{
                LoginDestination()
            }
        }
    }
}