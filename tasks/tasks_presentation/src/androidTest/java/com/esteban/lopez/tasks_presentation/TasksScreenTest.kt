package com.esteban.ruano.tasks_presentation

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.tasks_presentation.navigation.tasksGraph
import com.esteban.ruano.tasks_presentation.robot.TasksRobot
import com.esteban.ruano.tasks_presentation.ui.TasksScreen
import com.esteban.ruano.test_core.base.AndroidTestActivity
import com.esteban.ruano.test_core.base.rules.lazyActivityScenarioRule
import com.esteban.ruano.test_core.server.ServerBasedTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TasksScreenTest : ServerBasedTest(){

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var activityScenarioRule = lazyActivityScenarioRule<AndroidTestActivity>(launchActivity = false)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule() as AndroidComposeTestRule<*, *>

    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
    }

    @Test
    fun testScreen() {
        with(TasksRobot(composeRule = composeTestRule, navController = navController)) {
            addTasksResponse()
            activityScenarioRule.launch()
            hiltRule.inject()

          with(activityScenarioRule.getScenario()){
                onActivity {
                    it.setContent {
                        NavHost(navController = navController, startDestination = Routes.BASE.TASKS.name) {
                            tasksGraph(
                                navController = navController,
                               )
                        }
                    }
                }

              testTasksScreen()
              testTaskList()
          }
        }
    }

}