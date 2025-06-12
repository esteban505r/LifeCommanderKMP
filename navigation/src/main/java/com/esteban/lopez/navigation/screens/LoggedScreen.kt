package com.esteban.ruano.navigation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.utils.IconUtils
import com.esteban.ruano.navigation.NavHostWrapper

@Composable
fun LoggedScreen(onRootNavigate: (String) -> Unit) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color.White,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val currentRoute = navBackStackEntry?.destination?.route

                Routes.BASE.getAllRoutes().forEach { screen ->
                    BottomNavigationItem(
                        unselectedContentColor = Gray,
                        selectedContentColor = Color.Black,
                        modifier = Modifier.height(75.dp),
                        icon = {
                            Image(
                                painter = painterResource(IconUtils.getResourceIconByString(screen.name)),
                                modifier = Modifier.padding(bottom = 6.dp).size(24.dp),
                                contentDescription = screen.name
                            )
                        },
                        label = { Text(text = screen.label ?: "") },
                        selected = currentRoute == screen.name,
                        onClick = {
                            navController.navigate(screen.name) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        NavHostWrapper(
            modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues()).padding(it),
            navController = navController,
            shouldShowOnboarding = false,
        )
    }
}