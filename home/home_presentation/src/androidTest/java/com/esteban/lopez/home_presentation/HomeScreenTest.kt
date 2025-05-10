package com.esteban.ruano.home_presentation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.navigation.graphs.baseGraph
import com.esteban.ruano.test_core.base.AndroidTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<AndroidTestActivity> ()
    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        hiltRule.inject()
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            navController.graph =
                navController.createGraph(startDestination = Routes.BASE.HOME.name) {
                    baseGraph(
                        navController = navController,
                        onRootNavigate = {
                            navController.navigate(it)
                        }
                    )
                }
            HomeScreen(onGoToTasks = {
                navController.navigate(Routes.BASE.TASKS.name)
            }, onGoToWorkout = {
                navController.navigate(Routes.BASE.WORKOUT.name)
            }, onCurrentHabitClick = {
                navController.navigate(Routes.BASE.HABITS.name)
            })
        }
    }

    @Before
    fun setUp() {

    }

    @Test
    fun testScreen() {
        with(
            com.esteban.ruano.home_presentation.robot.HomeRobot(
                composeRule = composeTestRule,
                navController = navController
            )
        ) {
            testHome()
        }
    }

}