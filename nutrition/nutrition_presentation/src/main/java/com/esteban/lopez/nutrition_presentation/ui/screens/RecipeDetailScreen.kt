package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.lopez.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.text.TitleH3
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.HomeColors
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.nutrition_domain.model.MealTag
import com.esteban.ruano.nutrition_presentation.intent.RecipeDetailIntent
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.RecipeDetailState

@Composable
fun RecipeDetailScreen(
    state: RecipeDetailState,
    onClose: (Boolean) -> Unit,
    userIntent: (RecipeDetailIntent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HomeColors.LightBackground
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = HomeColors.PrimaryBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            state.isError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading recipe",
                            style = MaterialTheme.typography.h6,
                            color = HomeColors.StatusHigh
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.body2,
                            color = HomeColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            state.recipe != null -> {
                RecipeDetailContent(
                    state = state,
                    onClose = onClose,
                    userIntent = userIntent
                )
            }
        }
    }
}

@Composable
private fun RecipeDetailContent(
    state: RecipeDetailState,
    onClose: (Boolean) -> Unit,
    userIntent: (RecipeDetailIntent) -> Unit,
) {
    val recipe = state.recipe!!
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { userIntent(RecipeDetailIntent.EditRecipe) },
                backgroundColor = HomeColors.PrimaryBlue
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit recipe",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
    Column(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            .verticalScroll(rememberScrollState())
        ) {
            val context = LocalContext.current
            // App Bar
            AppBar(
                title = recipe.name,
                onClose = { onClose(false) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { userIntent(RecipeDetailIntent.EditRecipe) }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit recipe",
                        tint = HomeColors.PrimaryBlue
                    )
                }
                IconButton(
                    onClick = { userIntent(RecipeDetailIntent.DeleteRecipe) }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete recipe",
                        tint = HomeColors.StatusHigh
                    )
                }
            }

            // Recipe Image (if available)
            recipe.image?.let { imageUrl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HomeColors.SurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder for image - in a real app, you'd use Coil or similar
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = "Recipe image",
                            modifier = Modifier.size(64.dp),
                            tint = HomeColors.TextTertiary
                        )
                    }
                }
            }

            // Recipe Info Cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 2.dp,
                    backgroundColor = HomeColors.CardBackground
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Recipe Summary",
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                color = HomeColors.TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Ingredients",
                                    style = MaterialTheme.typography.caption,
                                    color = HomeColors.TextSecondary
                                )
                                Text(
                                    text = "${recipe.ingredients.size} items",
                                    style = MaterialTheme.typography.body2.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HomeColors.TextPrimary
                                    )
                                )
                            }
                            Column {
                                Text(
                                    text = "Steps",
                                    style = MaterialTheme.typography.caption,
                                    color = HomeColors.TextSecondary
                                )
                                Text(
                                    text = "${recipe.instructions.size} steps",
                                    style = MaterialTheme.typography.body2.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HomeColors.TextPrimary
                                    )
                                )
                            }
                            Column {
                                Text(
                                    text = "Total Calories",
                                    style = MaterialTheme.typography.caption,
                                    color = HomeColors.TextSecondary
                                )
                                Text(
                                    text = "${recipe.calories ?: 0} kcal",
                                    style = MaterialTheme.typography.body2.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HomeColors.TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Basic Info Card
                InfoCard(
                    title = "Recipe Information",
                    content = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoRowBlock("Name", recipe.name)
                            recipe.note?.let { note ->
                                if (note.isNotBlank()) {
                                    InfoRowBlock("Notes", note, valueBold = false)
                                }
                            }
                            recipe.mealTag?.let { mealTag ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Meal Type",
                                        style = MaterialTheme.typography.caption.copy(
                                            color = HomeColors.TextSecondary,
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (mealTag.uppercase()) {
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
                                            text = MealTag.valueOf(mealTag).name,
                                            style = MaterialTheme.typography.body2.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = when (mealTag.uppercase()) {
                                                    "BREAKFAST" -> Color(0xFFFF8F00)
                                                    "LUNCH" -> Color(0xFF4CAF50)
                                                    "DINNER" -> Color(0xFF2196F3)
                                                    "SNACK" -> Color(0xFFFF5722)
                                                    else -> Color(0xFF757575)
                                                }
                                            )
                                        )
                                    }
                                }
                            }
                            recipe.days?.let { days ->
                                if (days.isNotEmpty()) {
                                    InfoRowBlock(
                                        label = "Scheduled Days",
                                        value = days.joinToString(", ") { day ->
                                            day.toDayOfTheWeekString(context)
                                        },
                                        valueBold = false
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nutrition Card
                NutritionCard(recipe)

                Spacer(modifier = Modifier.height(16.dp))

                // Ingredients Card
                if (recipe.ingredients.isNotEmpty()) {
                    IngredientsCard(recipe.ingredients)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Instructions Card
                if (recipe.instructions.isNotEmpty()) {
                    InstructionsCard(recipe.instructions)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom spacer for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = HomeColors.CardBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeColors.TextPrimary
                )
            )
        Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InfoRowBlock(
    label: String,
    value: String,
    valueBold: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption.copy(
                color = HomeColors.TextSecondary,
                fontWeight = FontWeight.Normal
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = if (valueBold) MaterialTheme.typography.body1.copy(
                color = HomeColors.TextPrimary,
                fontWeight = FontWeight.Medium
            ) else MaterialTheme.typography.body2.copy(
                color = HomeColors.TextPrimary
            )
        )
    }
}

@Composable
private fun NutritionCard(recipe: com.esteban.ruano.lifecommander.models.Recipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = HomeColors.CardBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nutrition Information",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Main nutrition values - using existing color scheme
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row: Protein and Calories
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionBadge("Protein", recipe.protein?.toString() ?: "0", Color(0xFF4CAF50))
                    NutritionBadge("Calories", recipe.calories?.toString() ?: "0", Color(0xFFFF9800))
                }

                // Second row: Carbs and Fat
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionBadge("Carbs", recipe.carbs?.toString() ?: "0", Color(0xFF2196F3))
                    NutritionBadge("Fat", recipe.fat?.toString() ?: "0", Color(0xFF9C27B0))
                }

                // Third row: Fiber and Sugar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionBadge("Fiber", recipe.fiber?.toString() ?: "0", Color(0xFF795548))
                    NutritionBadge("Sugar", recipe.sugar?.toString() ?: "0", Color(0xFFE91E63))
                }
            }
        }
    }
}

@Composable
private fun NutritionBadge(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$value${if (label != "Calories") "g" else ""} $label",
            style = MaterialTheme.typography.body2.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun IngredientsCard(ingredients: List<com.esteban.ruano.lifecommander.models.Ingredient>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = HomeColors.CardBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bullet point
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(HomeColors.PrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    // Ingredient info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = ingredient.name,
                            style = MaterialTheme.typography.body2,
                            color = HomeColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${ingredient.quantity} ${ingredient.unit}",
                            style = MaterialTheme.typography.caption,
                            color = HomeColors.TextSecondary
                        )
                    }

                    // Quantity badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HomeColors.PrimaryBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${ingredient.quantity} ${ingredient.unit}",
                            style = MaterialTheme.typography.caption.copy(
                                fontWeight = FontWeight.Bold,
                                color = HomeColors.PrimaryBlue
                            )
                        )
                    }
                }

                if (index < ingredients.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = HomeColors.SurfaceBorder
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionsCard(instructions: List<com.esteban.ruano.lifecommander.models.Instruction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = HomeColors.CardBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            instructions.sortedBy { it.stepNumber }.forEach { instruction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Step number
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(HomeColors.PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = instruction.stepNumber.toString(),
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // Instruction text
                    Text(
                        text = instruction.description,
                        style = MaterialTheme.typography.body2,
                        color = HomeColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (instruction.stepNumber < instructions.size) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = HomeColors.SurfaceBorder
                    )
                }
            }
        }
    }
}


