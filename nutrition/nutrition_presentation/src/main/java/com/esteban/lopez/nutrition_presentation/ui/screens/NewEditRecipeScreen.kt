package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.Gray2
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.nutrition_domain.model.MealTag
import com.esteban.ruano.nutrition_presentation.intent.NewEditRecipeIntent
import kotlinx.coroutines.launch
import com.esteban.ruano.core_ui.composables.text.TitleH4 as CoreTitleH4

@Composable
fun NewEditRecipeScreen(
    recipeToEdit: Recipe?,
    onClose: (Boolean) -> Unit,
    userIntent: (NewEditRecipeIntent) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf(recipeToEdit?.name ?: "") }
    val notes = remember { mutableStateOf(recipeToEdit?.note ?: "") }
    val protein = remember { mutableStateOf(recipeToEdit?.protein?.toString() ?: "") }
    var selectedDays by remember { mutableStateOf(recipeToEdit?.days ?: emptyList()) }
    val mealTag = remember { mutableStateOf(recipeToEdit?.mealTag?.let { try { MealTag.valueOf(it) } catch (e: Exception) { null } } ?: null) }
    val dropDownExpanded = remember { mutableStateOf(false) }
    val mealDropDownExpanded = remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    
    LaunchedEffect (recipeToEdit){
        if(recipeToEdit!=null){
            name = recipeToEdit.name
            notes.value = recipeToEdit.note?:""
            protein.value = recipeToEdit.protein.toString()
            selectedDays = recipeToEdit.days ?: emptyList()
            mealTag.value = try {
                MealTag.valueOf(recipeToEdit.mealTag!!)
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        AppBar(
            if (recipeToEdit != null) stringResource(id = R.string.edit_recipe_title) else stringResource(
                id = R.string.new_recipe_title
            ),
            onClose = {
                onClose(false)
            }
        ) {
            TextButton(onClick = {
                if(protein.value.toDoubleOrNull()==null){
                    return@TextButton
                }
                coroutineScope.launch {
                    if (recipeToEdit != null) {
                        userIntent(
                            NewEditRecipeIntent.UpdateRecipe(
                                recipeToEdit.id!!,
                                recipeToEdit.copy(
                                    name = name,
                                    note = notes.value,
                                    protein = protein.value.toDouble(),
                                    days = selectedDays,
                                    mealTag = mealTag.value?.name ?: ""
                                ),
                            )
                        )
                    } else {
                        userIntent(
                            NewEditRecipeIntent.CreateRecipe(
                                name = name,
                                note = notes.value,
                                protein = protein.value.toDouble(),
                                days = selectedDays,
                                mealTag =  mealTag.value?.name ?: ""
                            )
                        )
                    }
                }
            }) {
                Text(stringResource(id = R.string.save))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.name)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = notes.value,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            shape = RoundedCornerShape(12.dp),
            label = { Text(stringResource(id = R.string.add_notes)) },
            onValueChange = { notes.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            value = protein.value,
            onValueChange = { protein.value = it },
            label = { Text(stringResource(id = R.string.protein)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        CoreTitleH4(R.string.days, modifier = Modifier.padding(start = 8.dp))
        Box(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Column {
                // Show selected days
                if (selectedDays.isNotEmpty()) {
                    Text(
                        text = selectedDays.joinToString(", ") { it.toDayOfTheWeekString(context) },
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    Text(
                        text = stringResource(R.string.dont_assign),
                        style = MaterialTheme.typography.body1
                    )
                }
                
                // Day selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..7) {
                        val dayName = when (i) {
                            1 -> "M"
                            2 -> "T"
                            3 -> "W"
                            4 -> "T"
                            5 -> "F"
                            6 -> "S"
                            7 -> "S"
                            else -> ""
                        }
                        
                        val isSelected = selectedDays.contains(i)
                        Button(
                            onClick = {
                                if (isSelected) {
                                    selectedDays = selectedDays.filter { it != i }
                                } else {
                                    selectedDays = selectedDays + i
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                                contentColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                            ),
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape
                        ) {
                            Text(dayName, style = MaterialTheme.typography.caption)
                        }
                    }
                }
                
                // Clear all button
                TextButton(
                    onClick = { selectedDays = emptyList() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.clear_all))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CoreTitleH4(R.string.meal, modifier = Modifier.padding(start = 8.dp))
        Box(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(if(mealTag.value==null) stringResource(R.string.dont_assign) else mealTag.value!!.name)
                IconButton(onClick = {
                    mealDropDownExpanded.value = true
                }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "More options")
                }
            }
            DropdownMenu(
                expanded = mealDropDownExpanded.value,
                onDismissRequest = { mealDropDownExpanded.value = false },
            ) {
                for (i in MealTag.entries) {
                    DropdownMenuItem(
                        onClick = {
                            mealTag.value = i
                            mealDropDownExpanded.value = false
                        }
                    ) {
                        Text(i.name)
                    }
                }

                DropdownMenuItem(
                    onClick = {
                        mealTag.value = null
                        mealDropDownExpanded.value = false
                    }
                ) {
                    Text(stringResource(id = R.string.dont_assign))
                }
            }
        }
    }
}


