package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.ui.state.RecipesState
import java.time.DayOfWeek
import ui.composables.NewEditRecipeDialog
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipesScreen(
    state: RecipesState,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit,
    onGetRecipesByDay: (Int) -> Unit,
    onGetAllRecipes: () -> Unit = {},
    onEditRecipe: (Recipe) -> Unit = {},
    onDeleteRecipe: (String) -> Unit = {},
    onConsumeRecipe: (String) -> Unit = {},
    onSkipRecipe: (String) -> Unit = {},
    onSkipRecipeWithAlternative: (String, String?, String?) -> Unit ,
    modifier: Modifier = Modifier
) {
    var showRecipeDialog by remember { mutableStateOf(false) }
    var recipeToEdit by remember { mutableStateOf<Recipe?>(null) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf<Recipe?>(null) }
    var showAlternativeDialog by remember { mutableStateOf<Recipe?>(null) }
    var alternativeMealName by remember { mutableStateOf(TextFieldValue("")) }
    
    val daysOfWeek = DayOfWeek.values()
    val selectedDay = state.daySelected
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Add Recipe button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedDay) {
                    -1 -> "Recipe Database"
                    0 -> "All Meals"
                    else -> "Meals by Day"
                },
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Button(
                onClick = {
                    recipeToEdit = null
                    showRecipeDialog = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Recipe")
            }
        }
        
        // Enhanced Chips Row with Database option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Database chip
            FilterChip(
                selected = selectedDay == -1,
                onClick = { onGetAllRecipes() },
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = if (selectedDay == -1) MaterialTheme.colors.secondary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Storage, contentDescription = "Database", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Database")
            }
            
            // Day chips
            daysOfWeek.forEach { day ->
                FilterChip(
                    selected = selectedDay == day.value,
                    onClick = { onGetRecipesByDay(day.value) },
                    colors = ChipDefaults.filterChipColors(
                        backgroundColor = if (selectedDay == day.value) MaterialTheme.colors.primary.copy(alpha = 0.15f) else MaterialTheme.colors.surface,
                        contentColor = MaterialTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(day.name)
                }
            }
        }
        
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = "No meals",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        when (selectedDay) {
                            -1 -> "No recipes in database"
                            0 -> "No meals found"
                            else -> "No meals found for this day"
                        },
                        style = MaterialTheme.typography.body1.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.recipes.size) { index ->
                    val recipe = state.recipes[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (selectedDay == -1) 120.dp else 160.dp),
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (recipe.consumed) 
                            MaterialTheme.colors.surface.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colors.surface
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
                                    .size(72.dp)
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
                                    modifier = Modifier.size(36.dp)
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
                                // Recipe name and status
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            recipe.name,
                                            style = MaterialTheme.typography.h6.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (recipe.consumed) 
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                else 
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.95f)
                                            ),
                                            maxLines = 1
                                        )
                                        if (recipe.consumed) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Consumed",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    
                                    if (!recipe.note.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            recipe.note!!,
                                            style = MaterialTheme.typography.body2.copy(
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    
                                    // Show day assignment for database recipes
                                    if (selectedDay == -1 && recipe.day != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Assigned to ${DayOfWeek.of(recipe.day!!).name}",
                                            style = MaterialTheme.typography.caption.copy(
                                                color = MaterialTheme.colors.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                                
                                // Bottom row with badges and actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Badges row
                                    Row(
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
                                                text = "${recipe.protein ?: 0}g protein",
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
                                                text = recipe.mealTag ?: "Unknown",
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
                                    
                                    // Action buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Only show meal tracking buttons for day-specific views (not database)
                                        if (selectedDay != -1 && selectedDay != 0 && selectedDay > 0) {
                                            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                                            val todayIndex = now.dayOfWeek.value
                                            if (selectedDay == todayIndex) {
                                                // Show action buttons (consume, skip, edit, delete) ONLY for today
                                                // Consume Recipe Button
                                                IconButton(
                                                    onClick = { onConsumeRecipe(recipe.id) },
                                                    modifier = Modifier.size(36.dp),
                                                    enabled = !recipe.consumed
                                                ) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = if (recipe.consumed) "Already Consumed" else "Consume Recipe",
                                                        tint = if (recipe.consumed) 
                                                            MaterialTheme.colors.onSurface.copy(alpha = 0.3f) 
                                                        else 
                                                            Color(0xFF4CAF50)
                                                    )
                                                }
                                                
                                                // Skip Recipe Button
                                                IconButton(
                                                    onClick = { showSkipDialog = recipe },
                                                    modifier = Modifier.size(36.dp),
                                                    enabled = !recipe.consumed
                                                ) {
                                                    Icon(
                                                        Icons.Default.SkipNext,
                                                        contentDescription = "Skip Recipe",
                                                        tint = if (recipe.consumed) 
                                                            MaterialTheme.colors.onSurface.copy(alpha = 0.3f) 
                                                        else 
                                                            Color(0xFFFF9800)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                recipeToEdit = recipe
                                                showRecipeDialog = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Recipe",
                                                tint = MaterialTheme.colors.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                recipeToDelete = recipe
                                                showDeleteDialog = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Recipe",
                                                tint = MaterialTheme.colors.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Recipe Dialog
    NewEditRecipeDialog(
        recipeToEdit = recipeToEdit,
        show = showRecipeDialog,
        onDismiss = {
            showRecipeDialog = false
            recipeToEdit = null
        },
        onSave = { recipe ->
            if (recipeToEdit == null) {
                onNewRecipe()
            } else {
                onEditRecipe(recipe)
            }
            showRecipeDialog = false
            recipeToEdit = null
        }
    )

    // Delete Warning Dialog
    if (showDeleteDialog && recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete '${recipeToDelete!!.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecipe(recipeToDelete!!.id)
                        showDeleteDialog = false
                        recipeToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        recipeToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Skip Recipe Dialog
    showSkipDialog?.let { recipe ->
        var searchQuery by remember { mutableStateOf("") }
        var filteredRecipes = state.recipes.filter {
            it.id != recipe.id && (it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isBlank())
        }
        var expanded by remember { mutableStateOf(false) }
        var selectedReplacement: Recipe? by remember { mutableStateOf(null) }
        AlertDialog(
            onDismissRequest = { showSkipDialog = null },
            title = { Text("Skip ${recipe.name}") },
            text = {
                Column {
                    Text("You must specify what you ate instead.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search recipes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedReplacement?.name ?: "Select replacement recipe")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredRecipes.forEach { r ->
                                DropdownMenuItem(onClick = {
                                    selectedReplacement = r
                                    alternativeMealName = TextFieldValue("")
                                    expanded = false
                                }) {
                                    Text(r.name)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Or enter a custom meal name:")
                    TextField(
                        value = alternativeMealName,
                        onValueChange = {
                            alternativeMealName = it
                            if (it.text.isNotBlank()) selectedReplacement = null
                        },
                        label = { Text("Replacement meal name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = alternativeMealName.text.isBlank() && selectedReplacement == null
                    )
                    if (alternativeMealName.text.isBlank() && selectedReplacement == null) {
                        Text("Replacement meal is required", color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSkipRecipeWithAlternative(
                            recipe.id,
                            selectedReplacement?.id,
                            if (selectedReplacement == null) alternativeMealName.text else null
                        )
                        showSkipDialog = null
                        alternativeMealName = TextFieldValue("")
                        selectedReplacement = null
                        searchQuery = ""
                    },
                    enabled = selectedReplacement != null || alternativeMealName.text.isNotBlank()
                ) {
                    Text("Skip")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSkipDialog = null
                        alternativeMealName = TextFieldValue("")
                        selectedReplacement = null
                        searchQuery = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 