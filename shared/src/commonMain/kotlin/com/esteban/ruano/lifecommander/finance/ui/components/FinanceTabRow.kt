package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.state.FinanceState
import kotlinx.coroutines.launch

expect object PlatformConfiguration {
    val isLandscape: Boolean
}

data class FinanceTabItem(
    val title: String,
    val icon: @Composable () -> Unit,
    val action: () -> Unit
)

@Composable
fun FinanceTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    actions: List<FinanceTabItem>,
    isDesktop: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val isLandscape = PlatformConfiguration.isLandscape

    if (isDesktop || isLandscape) {
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colors.onPrimary,
                    height = 3.dp
                )
            }
        ) {
            actions.forEachIndexed { index, item ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        onTabSelected(index)
                        scope.launch {
                            item.action()
                        }
                    },
                    text = {
                        Text(
                            item.title,
                            color = if (selectedTab == index) 
                                MaterialTheme.colors.onPrimary 
                            else 
                                MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                        )
                    },
                    icon = {
                        item.icon()
                    },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        ScrollableTabRow(
            edgePadding = 16.dp,
            selectedTabIndex = selectedTab,
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colors.onPrimary,
                    height = 3.dp
                )
            }
        ) {
            actions.forEachIndexed { index, item ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        onTabSelected(index)
                        scope.launch {
                            item.action()
                        }
                    },
                    text = {
                        Text(
                            item.title,
                            color = if (selectedTab == index) 
                                MaterialTheme.colors.onPrimary 
                            else 
                                MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                        )
                    },
                    icon = {
                        item.icon()
                    },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
} 