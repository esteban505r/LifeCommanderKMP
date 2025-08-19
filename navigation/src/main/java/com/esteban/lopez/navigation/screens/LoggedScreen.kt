package com.esteban.lopez.navigation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.esteban.ruano.core_ui.utils.SystemBarUtils
import com.esteban.ruano.navigation.NavHostWrapper

@Composable
fun LoggedScreen(onRootNavigate: (String) -> Unit) {
    val navController = rememberNavController()
    val bottomNavColor = Color.White
    
    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = bottomNavColor,
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
    ) { padding ->
        // Modern approach: Use utility function for consistent system bar handling
        NavHostWrapper(
            modifier = Modifier.padding(padding),
            navController = navController,
            shouldShowOnboarding = false,
        )
        
        // Alternative approaches you can try:
        
        // 1. Manual approach with WindowInsets:
        // NavHostWrapper(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(bottomNavColor)
        //         .padding(WindowInsets.systemBars.asPaddingValues())
        //         .padding(it),
        //     navController = navController,
        //     shouldShowOnboarding = false,
        // )
        
        // 2. Separate status and navigation bar handling:
        // NavHostWrapper(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(bottomNavColor)
        //         .padding(WindowInsets.statusBars.asPaddingValues())
        //         .padding(WindowInsets.navigationBars.asPaddingValues())
        //         .padding(it),
        //     navController = navController,
        //     shouldShowOnboarding = false,
        // )
        
        // 3. Using utility functions:
        // NavHostWrapper(
        //     modifier = SystemBarUtils.withSystemBarBackground(
        //         backgroundColor = bottomNavColor,
        //         modifier = Modifier.padding(it)
        //     ),
        //     navController = navController,
        //     shouldShowOnboarding = false,
        // )
    }
}