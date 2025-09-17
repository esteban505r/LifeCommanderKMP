package com.esteban.lopez.nutrition_presentation.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.Recipe
import kotlin.collections.emptyList

data class SkippedMealResult(
    val selectedRecipeId: String? = null,
    val selectedRecipeName: String? = null,
    val manualName: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null,
    val sugar: Double? = null,
)

@Composable
fun SkipMealDialog(
    recipeTitle: String,
    onDismiss: () -> Unit,
    onSkip: (SkippedMealResult) -> Unit,
    onSearch: (String) -> Unit,
    isSearchLoading: Boolean,
    searchRecipes: List<Recipe>,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var manualName by rememberSaveable { mutableStateOf("") }

    // Selected recipe from suggestions
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    var calories by rememberSaveable { mutableStateOf("") }
    var protein  by rememberSaveable { mutableStateOf("") }
    var carbs    by rememberSaveable { mutableStateOf("") }
    var fat      by rememberSaveable { mutableStateOf("") }
    var fiber    by rememberSaveable { mutableStateOf("") }
    var sodium    by rememberSaveable { mutableStateOf("") }
    var sugar    by rememberSaveable { mutableStateOf("") }

    // Suggestions

    // Debounced search
    LaunchedEffect(search) {
        selectedRecipe = null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Skip $recipeTitle", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("You must specify what you ate instead.", color = MaterialTheme.colors.onSurface.copy(alpha = 0.75f))

                Spacer(Modifier.height(16.dp))

                // Search
                OutlinedTextField(
                    value = if (selectedRecipe != null) selectedRecipe!!.name else search,
                    onValueChange = {
                        search = it
                        onSearch(it)
                        if (selectedRecipe != null) selectedRecipe = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search recipes") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (selectedRecipe != null) {
                            IconButton(onClick = { selectedRecipe = null; search = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear selection")
                            }
                        }
                    },
                    enabled = selectedRecipe == null // lock the field if selected
                )

                // Suggestions list
                if (selectedRecipe == null && (isSearchLoading || searchRecipes.isNotEmpty())) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column {
                            if (isSearchLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            searchRecipes.forEach { item ->
                                SuggestionRow(
                                    item = item,
                                    onClick = {
                                        selectedRecipe = it
                                        search = ""
                                        manualName = ""
                                    }
                                )
                                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Manual name (used only if no recipe selected)
                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Or enter meal name") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    enabled = selectedRecipe == null
                )

                Spacer(Modifier.height(18.dp))
                Text(
                    "Add nutrition (optional, decimals allowed):",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(10.dp))

                NutritionRow(
                    leftLabel = "Calories", leftValue = calories, onLeftChange = { calories = it.onlyDecimal() },
                    rightLabel = "Protein (g)", rightValue = protein, onRightChange = { protein = it.onlyDecimal() }
                )
                Spacer(Modifier.height(10.dp))
                NutritionRow(
                    leftLabel = "Carbs (g)", leftValue = carbs, onLeftChange = { carbs = it.onlyDecimal() },
                    rightLabel = "Fat (g)", rightValue = fat, onRightChange = { fat = it.onlyDecimal() }
                )
                Spacer(Modifier.height(10.dp))
                NutritionRow(
                    leftLabel = "Fiber (g)", leftValue = fiber, onLeftChange = { fiber = it.onlyDecimal() },
                    rightLabel = "Sugar (g)", rightValue = sugar, onRightChange = { sugar = it.onlyDecimal() }
                )
                Spacer(Modifier.height(10.dp))
                NutritionRowSingle(
                    label = "Sodium (g)", value = sodium, onValueChange = { sodium = it.onlyDecimal() },
                )

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSkip(
                                SkippedMealResult(
                                    selectedRecipeId = selectedRecipe?.id,
                                    selectedRecipeName = selectedRecipe?.name,
                                    manualName = if (selectedRecipe == null) manualName.ifBlank { null } else null,
                                    calories = calories.toDoubleOrNull(),
                                    protein  = protein.toDoubleOrNull(),
                                    carbs    = carbs.toDoubleOrNull(),
                                    fat      = fat.toDoubleOrNull(),
                                    fiber    = fiber.toDoubleOrNull(),
                                    sugar    = sugar.toDoubleOrNull(),
                                    sodium = sodium.toDoubleOrNull())
                            )
                        },
                        enabled = selectedRecipe != null || manualName.isNotBlank()
                    ) { Text("Skip") }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(item: Recipe, onClick: (Recipe) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.body1, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!item.note.isNullOrBlank()) {
                Text(item.note!!, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun NutritionRowSingle(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.onlyDecimal()) },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        )
    )
}


@Composable
private fun NutritionRow(
    leftLabel: String,
    leftValue: String,
    onLeftChange: (String) -> Unit,
    rightLabel: String,
    rightValue: String,
    onRightChange: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = leftValue,
            onValueChange = onLeftChange,
            modifier = Modifier
                .weight(1f),
            placeholder = { Text(leftLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = rightValue,
            onValueChange = onRightChange,
            modifier = Modifier
                .weight(1f),
            placeholder = { Text(rightLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
    }
}
private fun String.onlyDecimal(): String {
    var out = this.filter { it.isDigit() || it == '.' }
    val firstDot = out.indexOf('.')
    if (firstDot != -1) out = out.substring(0, firstDot + 1) + out.substring(firstDot + 1).replace(".", "")
    return out
}
