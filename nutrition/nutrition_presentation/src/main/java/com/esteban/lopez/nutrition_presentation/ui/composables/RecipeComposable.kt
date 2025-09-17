package com.esteban.ruano.nutrition_presentation.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.nutrition_presentation.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipeComposable(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onOpen: (Recipe) -> Unit = {},
    onSkip: (Recipe) -> Unit = {},
    onUndo: (String) -> Unit = {},
    onConsume: (Recipe) -> Unit = {},
    onEdit: (Recipe) -> Unit = {},
    onDelete: ((Recipe) -> Unit)? = null,
) {
    val (accent, accentBg) = mealColors(recipe.mealTag)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        onClick = { onOpen.invoke(recipe) },
    ) {
        Column(Modifier.fillMaxWidth()) {

            // TOP: icon + title + chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (recipe.mealTag?.uppercase()) {
                            "BREAKFAST" -> Icons.Default.Restaurant
                            "LUNCH" -> Icons.Default.LocalDining
                            "DINNER" -> Icons.Default.Fastfood
                            "SNACK" -> Icons.Default.EmojiFoodBeverage
                            else -> Icons.Default.Fastfood
                        },
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = recipe.name,
                        maxLines = 2,
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold),
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipMini(
                            text = "${recipe.protein}g protein",
                            fg = Color(0xFF2E7D32),
                            bg = Color(0xFF2E7D32).copy(alpha = 0.12f),
                            leading = Icons.Default.FitnessCenter
                        )
                        ChipMini(
                            text = (recipe.mealTag ?: stringResource(R.string.no_meal))
                                .lowercase().replaceFirstChar { it.titlecase() },
                            fg = accent,
                            bg = accentBg,
                            leading = Icons.Default.Schedule
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(!recipe.consumed){
                    ActionIconButton(
                        icon = Icons.Default.CheckCircle,
                        tint = Color(0xFF2E7D32),
                        contentDesc = "Mark done",
                        onClick = { onConsume(recipe) }
                    )
                    ActionIconButton(
                        icon = Icons.Default.SkipNext,
                        tint = Color(0xFFF5A623),
                        contentDesc = "Skip",
                        onClick = { onSkip.invoke(recipe) },
                    )
                }
                else if(recipe.consumedTrackId!=null){
                    ActionIconButton(
                        icon = Icons.Default.Undo,
                        tint = Color(0xFFF5A623),
                        contentDesc = "Undo",
                        onClick = { onUndo.invoke(recipe.consumedTrackId!!) },
                    )
                }
                ActionIconButton(
                    icon = Icons.Default.Edit,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    contentDesc = "Edit",
                    onClick = { onEdit(recipe) }
                )
                onDelete?.let {
                    ActionIconButton(
                        icon = Icons.Default.Delete,
                        tint = Color(0xFFD32F2F),
                        contentDesc = "Delete",
                        onClick = { onDelete(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    enabled: Boolean = true,
    icon: ImageVector,
    tint: Color,
    contentDesc: String,
    onClick: () -> Unit
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier
            .size(48.dp) // phone-friendly target
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ChipMini(
    text: String,
    fg: Color,
    bg: Color,
    leading: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = fg,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 4.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.caption.copy(
                color = fg,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun mealColors(tag: String?): Pair<Color, Color> {
    val fg = when (tag?.uppercase()) {
        "BREAKFAST" -> Color(0xFFFF8F00)
        "LUNCH" -> Color(0xFF4CAF50)
        "DINNER" -> Color(0xFF2196F3)
        "SNACK" -> Color(0xFFFF5722)
        else -> Color(0xFF757575)
    }
    return fg to fg.copy(alpha = 0.14f)
}

