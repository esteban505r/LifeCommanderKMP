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
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
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
    onTabSelected: (FinanceTab) -> Unit,
    actions: List<FinanceTabItem>,
    isDesktop: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val isLandscape = PlatformConfiguration.isLandscape

    if (isDesktop || isLandscape) {
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colors.primary,
                    height = 3.dp
                )
            }
        ) {
            actions.forEachIndexed { index, item ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        onTabSelected(FinanceTab.fromIndex(index))
                        scope.launch {
                            item.action()
                        }
                    },
                    text = {
                        Text(
                            item.title,
                            color = if (selectedTab == index) 
                                MaterialTheme.colors.primary
                            else 
                                MaterialTheme.colors.primary.copy(alpha = 0.6f)
                        )
                    },
                    icon = {
                        item.icon()
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.primary.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        ScrollableTabRow(
            edgePadding = 16.dp,
            selectedTabIndex = selectedTab,
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colors.primary,
                    height = 3.dp
                )
            }
        ) {
            actions.forEachIndexed { index, item ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        onTabSelected(FinanceTab.fromIndex(index))
                        scope.launch {
                            item.action()
                        }
                    },
                    text = {
                        Text(
                            item.title,
                            color = if (selectedTab == index) 
                                MaterialTheme.colors.primary
                            else 
                                MaterialTheme.colors.primary.copy(alpha = 0.6f)
                        )
                    },
                    icon = {
                        item.icon()
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
} 