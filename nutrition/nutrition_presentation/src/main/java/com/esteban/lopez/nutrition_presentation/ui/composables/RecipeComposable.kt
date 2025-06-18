package com.esteban.ruano.nutrition_presentation.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.core_ui.composables.ListTile
import com.esteban.ruano.core_ui.theme.LightGray4
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_presentation.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipeComposable(
    recipe: Recipe,
    showDay: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(120.dp),
        elevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe icon with improved styling
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        when (recipe.mealTag?.uppercase()) {
                            "BREAKFAST" -> Color(0xFFFFB74D)
                            "LUNCH" -> Color(0xFF81C784)
                            "DINNER" -> Color(0xFF64B5F6)
                            "SNACK" -> Color(0xFFFF8A65)
                            else -> Color(0xFFE0E0E0)
                        }.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (recipe.mealTag?.uppercase()) {
                        "BREAKFAST" -> Icons.Default.Restaurant
                        "LUNCH" -> Icons.Default.LocalDining
                        "DINNER" -> Icons.Default.Fastfood
                        else -> Icons.Default.Fastfood
                    },
                    contentDescription = "Food",
                    tint = when (recipe.mealTag?.uppercase()) {
                        "BREAKFAST" -> Color(0xFFFF8F00)
                        "LUNCH" -> Color(0xFF4CAF50)
                        "DINNER" -> Color(0xFF2196F3)
                        "SNACK" -> Color(0xFFFF5722)
                        else -> Color(0xFF757575)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Recipe name and protein info
                Column {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.95f)
                        ),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Protein badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${recipe.protein}g protein",
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        
                        // Meal type badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (recipe.mealTag?.uppercase()) {
                                        "BREAKFAST" -> Color(0xFFFFB74D).copy(alpha = 0.15f)
                                        "LUNCH" -> Color(0xFF81C784).copy(alpha = 0.15f)
                                        "DINNER" -> Color(0xFF64B5F6).copy(alpha = 0.15f)
                                        "SNACK" -> Color(0xFFFF8A65).copy(alpha = 0.15f)
                                        else -> Color(0xFFE0E0E0).copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = recipe.mealTag ?: stringResource(R.string.no_meal),
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = when (recipe.mealTag?.uppercase()) {
                                        "BREAKFAST" -> Color(0xFFFF8F00)
                                        "LUNCH" -> Color(0xFF4CAF50)
                                        "DINNER" -> Color(0xFF2196F3)
                                        "SNACK" -> Color(0xFFFF5722)
                                        else -> Color(0xFF757575)
                                    },
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
                
                // Bottom row with day and additional info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Day information
                    if (showDay && recipe.day != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Day:",
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = recipe.day.toDayOfTheWeekString(LocalContext.current),
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colors.primary
                                )
                            )
                        }
                    } else if (showDay) {
                        Text(
                            text = stringResource(com.esteban.ruano.core_ui.R.string.dont_assign),
                            style = MaterialTheme.typography.caption.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    // Recipe ID or additional info
                    Text(
                        text = "ID: ${recipe.id}",
                        style = MaterialTheme.typography.caption.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}