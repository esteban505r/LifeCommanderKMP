package com.esteban.ruano.tasks_presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.tasks_presentation.ui.NewTaskScreen
import com.esteban.ruano.tasks_presentation.ui.TaskDetailScreen

fun NavGraphBuilder.tasksGraph(
    navController: NavController
) {
    composable(Routes.TASKS.name) {
        TasksDestination(navController = navController)
    }
    //Tasks
    composable("${Routes.TASK_DETAIL}/{taskId}") {
        val taskId = it.arguments?.getString("taskId")!!
        TaskDetailScreen(taskId, onNavigateUp = {
            navController.navigateUp()
        }, onEditClick = {
            navController.navigate("${Routes.NEW_EDIT_TASK}/$taskId")
        })
    }
    composable(Routes.NEW_EDIT_TASK) {
        NewTaskScreen(onClose = {
            navController.navigateUp()
        })
    }
    composable("${Routes.NEW_EDIT_TASK}/{taskId}") {
        NewTaskScreen(onClose = {
            navController.navigateUp()
        }, taskToEditId = it.arguments?.getString("taskId")!!)
    }
}