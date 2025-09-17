package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.ui.*

/**
 * Cross-Platform Shared UI Components
 * 
 * These components work across all platforms (Android, iOS, Web, Desktop)
 * and use the centralized design system.
 */

@Composable
fun SharedAppBar(
    titleIcon: Painter,
    title: String,
    titleSize: TextUnit? = null,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        elevation = LifeCommanderDesignSystem.dimensions.ElevationNone,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = LifeCommanderDesignSystem.dimensions.PaddingContent,
                    vertical = LifeCommanderDesignSystem.dimensions.PaddingLarge
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = titleIcon,
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(
                    modifier.width(12.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold,
                        color = LifeCommanderDesignSystem.colors.OnSurface,
                        fontSize = titleSize?: MaterialTheme.typography.h4.fontSize
                    )
                )
            }

            Row {
                actions()
            }
        }
    }
}

@Composable
fun SharedWelcomeCard(
    greeting: String,
    subtitle: String,
    habitName: String?,
    habitSubtitle: String,
    onHabitClick: () -> Unit,
    modifier: Modifier = Modifier,
    mascotContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                LifeCommanderDesignSystem.ComponentPresets.WelcomeCardElevation,
                LifeCommanderDesignSystem.ComponentPresets.WelcomeCardShape
            ),
        shape = LifeCommanderDesignSystem.ComponentPresets.WelcomeCardShape,
        backgroundColor = LifeCommanderDesignSystem.ComponentPresets.WelcomeCardBackgroundColor,
        elevation = LifeCommanderDesignSystem.dimensions.ElevationNone
    ) {
        Box(
            modifier = Modifier
                .background(LifeCommanderDesignSystem.createGradientBrush(LifeCommanderDesignSystem.gradients.WelcomeGradient))
                .padding(LifeCommanderDesignSystem.ComponentPresets.WelcomeCardPadding)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.h5.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color.White
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.body1.copy(
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            ),
                            modifier = Modifier.padding(top = LifeCommanderDesignSystem.dimensions.SpacingSmall)
                        )
                    }
                    
                    // Mascot content - can be customized per platform
                    Box(
                        modifier = Modifier
                            .size(LifeCommanderDesignSystem.dimensions.AvatarHuge)
                            .clip(CircleShape)
                            .background(
                                LifeCommanderDesignSystem.createGradientBrush(
                                    listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    LifeCommanderDesignSystem.GradientDirection.Radial
                                )
                            )
                            .border(
                                LifeCommanderDesignSystem.dimensions.BorderThick,
                                Color.White.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        if (mascotContent != null) {
                            mascotContent()
                        } else {
                            // Default fallback
                            Text(
                                text = "🧘",
                                fontSize = 32.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(LifeCommanderDesignSystem.dimensions.SpacingExtraLarge))
                
                SharedHabitQuickCard(
                    habitName = habitName,
                    subtitle = habitSubtitle,
                    onClick = onHabitClick
                )
            }
        }
    }
}

@Composable
fun SharedHabitQuickCard(
    habitName: String?,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = LifeCommanderDesignSystem.shapes.CardElevated,
        backgroundColor = Color.White.copy(alpha = 0.95f),
        elevation = LifeCommanderDesignSystem.dimensions.ElevationLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifeCommanderDesignSystem.dimensions.PaddingContent),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(LifeCommanderDesignSystem.createGradientBrush(LifeCommanderDesignSystem.gradients.SecondaryGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧘",
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(LifeCommanderDesignSystem.dimensions.SpacingLarge))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habitName ?: "Start your journey",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = LifeCommanderDesignSystem.colors.OnSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body2.copy(
                        color = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = LifeCommanderDesignSystem.dimensions.SpacingExtraSmall)
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = LifeCommanderDesignSystem.colors.OnSurfaceVariant,
                modifier = Modifier.size(LifeCommanderDesignSystem.dimensions.IconLarge)
            )
        }
    }
}

@Composable
fun SharedSectionCard(
    title: String,
    subtitle: String,
    iconColor: Color,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                LifeCommanderDesignSystem.ComponentPresets.SectionCardElevation,
                LifeCommanderDesignSystem.ComponentPresets.SectionCardShape
            ),
        shape = LifeCommanderDesignSystem.ComponentPresets.SectionCardShape,
        backgroundColor = LifeCommanderDesignSystem.ComponentPresets.SectionCardBackgroundColor,
        elevation = LifeCommanderDesignSystem.dimensions.ElevationNone
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifeCommanderDesignSystem.ComponentPresets.SectionCardPadding)
        ) {
            SharedSectionHeader(
                title = title,
                subtitle = subtitle,
                iconColor = iconColor,
                onClick = onHeaderClick,
                iconContent = iconContent
            )
            
            Spacer(modifier = Modifier.height(LifeCommanderDesignSystem.dimensions.SpacingLarge))
            content()
        }
    }
}

@Composable
fun SharedSectionHeader(
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(LifeCommanderDesignSystem.shapes.CardDefault)
            .background(iconColor.copy(alpha = 0.1f))
            .padding(LifeCommanderDesignSystem.dimensions.PaddingLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LifeCommanderDesignSystem.dimensions.SpacingMedium)
        ) {
            Box(
                modifier = Modifier
                    .size(LifeCommanderDesignSystem.dimensions.IconExtraExtraLarge)
                    .clip(CircleShape)
                    .background(
                        LifeCommanderDesignSystem.createGradientBrush(
                            listOf(iconColor, iconColor.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (iconContent != null) {
                    iconContent()
                } else {
                    // Default fallback
                    Box(
                        modifier = Modifier
                            .size(LifeCommanderDesignSystem.dimensions.IconMedium)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = LifeCommanderDesignSystem.colors.OnSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.caption.copy(
                        color = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                    )
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = LifeCommanderDesignSystem.colors.OnSurfaceVariant,
            modifier = Modifier.size(LifeCommanderDesignSystem.dimensions.IconMedium)
        )
    }
}

@Composable
fun SharedTaskCard(
    taskName: String,
    taskNote: String?,
    dueDate: String?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LifeCommanderDesignSystem.dimensions.SpacingSmall),
        shape = LifeCommanderDesignSystem.ComponentPresets.TaskCardShape,
        backgroundColor = LifeCommanderDesignSystem.ComponentPresets.TaskCardBackgroundColor,
        elevation = LifeCommanderDesignSystem.ComponentPresets.TaskCardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifeCommanderDesignSystem.ComponentPresets.TaskCardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SharedTaskStatusIcon(isCompleted = isCompleted)
            
            Spacer(modifier = Modifier.width(LifeCommanderDesignSystem.dimensions.SpacingMedium))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = taskName,
                    style = MaterialTheme.typography.body1.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) 
                            LifeCommanderDesignSystem.colors.OnSurfaceVariant 
                        else 
                            LifeCommanderDesignSystem.colors.OnSurface
                    ),
                    maxLines = 1
                )
                
                if (taskNote?.isNotEmpty() == true) {
                    Text(
                        text = taskNote,
                        style = MaterialTheme.typography.body2.copy(
                            color = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(top = LifeCommanderDesignSystem.dimensions.SpacingExtraSmall)
                    )
                }
                
                if (dueDate != null) {
                    SharedTaskMetadata(dueDate = dueDate)
                }
            }
        }
    }
}

@Composable
fun SharedTaskStatusIcon(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    var modifier = modifier
        .size(LifeCommanderDesignSystem.dimensions.IconExtraLarge)
        .clip(CircleShape)

    modifier =  if (isCompleted) {
       modifier.background(LifeCommanderDesignSystem.createGradientBrush(LifeCommanderDesignSystem.gradients.SuccessGradient))
    } else {
        modifier.background(LifeCommanderDesignSystem.colors.SurfaceVariant)
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isCompleted) 
                Icons.Outlined.CheckCircle 
            else 
                Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(LifeCommanderDesignSystem.dimensions.IconMedium),
            tint = if (isCompleted) Color.White else LifeCommanderDesignSystem.colors.OnSurfaceDisabled
        )
    }
}

@Composable
private fun SharedTaskMetadata(
    dueDate: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = LifeCommanderDesignSystem.dimensions.SpacingExtraSmall)
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(LifeCommanderDesignSystem.dimensions.IconSmall),
            tint = LifeCommanderDesignSystem.colors.OnSurfaceDisabled
        )
        Spacer(modifier = Modifier.width(LifeCommanderDesignSystem.dimensions.SpacingExtraSmall))
        Text(
            text = dueDate,
            style = MaterialTheme.typography.caption.copy(
                color = LifeCommanderDesignSystem.colors.OnSurfaceDisabled
            )
        )
    }
}

@Composable
fun SharedGradientButton(
    text: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(LifeCommanderDesignSystem.ComponentPresets.PrimaryButtonHeight),
        shape = LifeCommanderDesignSystem.ComponentPresets.PrimaryButtonShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = Color.White,
            disabledBackgroundColor = LifeCommanderDesignSystem.colors.ButtonDisabled,
            disabledContentColor = LifeCommanderDesignSystem.colors.ButtonOnDisabled
        ),
        elevation = ButtonDefaults.elevation(LifeCommanderDesignSystem.dimensions.ElevationNone),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) {
                        LifeCommanderDesignSystem.createGradientBrush(gradientColors)
                    } else {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(LifeCommanderDesignSystem.colors.ButtonDisabled, LifeCommanderDesignSystem.colors.ButtonDisabled)
                        )
                    },
                    LifeCommanderDesignSystem.ComponentPresets.PrimaryButtonShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.button.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (enabled) Color.White else LifeCommanderDesignSystem.colors.ButtonOnDisabled
            )
        }
    }
}

@Composable
fun SharedLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = LifeCommanderDesignSystem.colors.Primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(LifeCommanderDesignSystem.dimensions.IconExtraLarge),
        color = color,
        strokeWidth = LifeCommanderDesignSystem.dimensions.BorderMedium
    )
}

@Composable
fun SharedErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(LifeCommanderDesignSystem.dimensions.PaddingLarge),
        shape = LifeCommanderDesignSystem.shapes.CardDefault,
        backgroundColor = LifeCommanderDesignSystem.colors.StatusOverdueBackground,
        elevation = LifeCommanderDesignSystem.dimensions.ElevationSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifeCommanderDesignSystem.dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                color = LifeCommanderDesignSystem.colors.StatusOverdueForeground
            )
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(LifeCommanderDesignSystem.dimensions.SpacingMedium))
                SharedGradientButton(
                    text = "Retry",
                    gradientColors = LifeCommanderDesignSystem.gradients.ErrorGradient,
                    onClick = onRetry,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

// Utility functions moved to shared
@Composable
fun getSharedTaskCategoryColor(categoryId: String): Color {
    return when (categoryId.hashCode() % 8) {
        0 -> LifeCommanderDesignSystem.colors.CategoryBlue
        1 -> LifeCommanderDesignSystem.colors.CategoryGreen
        2 -> LifeCommanderDesignSystem.colors.CategoryYellow
        3 -> LifeCommanderDesignSystem.colors.CategoryRed
        4 -> LifeCommanderDesignSystem.colors.CategoryPurple
        5 -> LifeCommanderDesignSystem.colors.CategoryCyan
        6 -> LifeCommanderDesignSystem.colors.CategoryPink
        else -> LifeCommanderDesignSystem.colors.CategoryIndigo
    }
} 