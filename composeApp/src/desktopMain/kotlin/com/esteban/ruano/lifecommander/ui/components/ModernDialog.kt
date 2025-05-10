package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ui.theme.Shapes

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModernDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmButtonText: String = "OK",
    onConfirm: () -> Unit = onDismiss,
    dismissButtonText: String? = null,
    onDismissButton: () -> Unit = onDismiss
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedContent(
            targetState = true,
            transitionSpec = {
                fadeIn() with fadeOut()
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = Shapes.large,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h5,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (dismissButtonText != null) {
                            TextButton(
                                onClick = onDismissButton,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(dismissButtonText)
                            }
                        }
                        
                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = MaterialTheme.colors.onPrimary
                            )
                        ) {
                            Text(confirmButtonText)
                        }
                    }
                }
            }
        }
    }
} 