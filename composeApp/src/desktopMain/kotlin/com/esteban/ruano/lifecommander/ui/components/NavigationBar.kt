package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.theme.Shapes

sealed class Screen(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object Habits : Screen("habits", { Icon(Icons.Default.CheckCircle, "Habits") }, "Habits")
    object Timers : Screen("timers", { Icon(Icons.Default.AccountCircle, "Timers") }, "Timers")
    object Settings : Screen("settings", { Icon(Icons.Default.Settings, "Settings") }, "Settings")
}

@Composable
fun NavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val screens = listOf(Screen.Habits, Screen.Timers, Screen.Settings)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = Shapes.large,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            screens.forEach { screen ->
                NavigationItem(
                    screen = screen,
                    isSelected = currentRoute == screen.route,
                    onClick = { onNavigate(screen.route) }
                )
            }
        }
    }
}

@Composable
fun NavigationItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isSelected) 
                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    else 
                        Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            screen.icon()
        }
        
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = screen.label,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
} 