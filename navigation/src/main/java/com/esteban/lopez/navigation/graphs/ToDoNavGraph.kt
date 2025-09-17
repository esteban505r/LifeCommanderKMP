package com.esteban.ruano.habits_presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.tasks_presentation.navigation.TasksDestination
import com.esteban.ruano.tasks_presentation.navigation.tasksGraph
import kotlinx.coroutines.launch



// Graph
fun NavGraphBuilder.toDoGraph(
    navController: NavHostController,
) {
    navigation(
        route = Routes.BASE.TO_DO.name,
        startDestination = "${Routes.BASE.TO_DO.name}?tab={tab}"
    ) {
        composable(
            route = "${Routes.BASE.TO_DO.name}?tab={tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.StringType
                defaultValue = "tasks"
            })
        ) { entry ->
            val initial = entry.arguments?.getString("tab") ?: "tasks"
            ToDoTabsScreen(
                navController = navController,
                initialTab = if (initial.equals("habits", true)) ToDoTab.Habits else ToDoTab.Tasks
            )
        }
    }
}

enum class ToDoTab { Habits, Tasks }

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ToDoTabsScreen(
    navController: NavHostController,
    initialTab: ToDoTab = ToDoTab.Tasks,
) {
    val initialPage = if (initialTab == ToDoTab.Habits) 0 else 1
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colors.primary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Habits") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Tasks") }
            )
        }

        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> HabitsDestination(
                    navController = navController
                )
                1 -> TasksDestination(
                    navController = navController
                )
            }
        }
    }
}
