package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.theme.*

@Composable
fun TimerSettingsModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isLoopEnabled: Boolean,
    onLoopToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernModal(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = "Timer Settings",
        modifier = modifier,
        actions = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loop Timers",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
                
                Switch(
                    checked = isLoopEnabled,
                    onCheckedChange = onLoopToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                    )
                )
            }
            
            Text(
                text = "When enabled, timers will automatically restart after completion.",
                style = MaterialTheme.typography.caption,
                color = TextSecondary
            )
        }
    }
} 