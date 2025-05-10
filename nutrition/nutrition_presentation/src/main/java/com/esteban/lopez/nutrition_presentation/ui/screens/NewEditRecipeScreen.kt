package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.Gray2
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.text.TitleH4
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.nutrition_domain.model.MealTag
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_presentation.intent.NewEditRecipeIntent
import kotlinx.coroutines.launch


@Composable
fun NewEditRecipeScreen(
    recipeToEdit: Recipe?,
    onClose: (Boolean) -> Unit,
    userIntent: (NewEditRecipeIntent) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember {
        mutableStateOf(recipeToEdit?.name ?: "")
    }
    val notes = remember { mutableStateOf("") }
    val protein = remember { mutableStateOf("") }
    val day = remember { mutableStateOf<Int?>(null) }
    val mealTag = remember { mutableStateOf<MealTag?>(null) }
    val dropDownExpanded = remember { mutableStateOf(false) }
    val mealDropDownExpanded = remember { mutableStateOf(false) }


    LaunchedEffect (recipeToEdit){
        if(recipeToEdit!=null){
            name = recipeToEdit.name
            notes.value = recipeToEdit.note?:""
            protein.value = recipeToEdit.protein.toString()
            day.value = recipeToEdit.day
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
                                    day = day.value,
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
                                day = day.value,
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
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            ),
            value = protein.value,
            onValueChange = { protein.value = it },
            label = { Text(stringResource(id = R.string.protein)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TitleH4(R.string.day, modifier = Modifier.padding(start = 8.dp))
        Box(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(if(day.value==null) stringResource(R.string.dont_assign) else day.value!!.toDayOfTheWeekString(LocalContext.current))
                IconButton(onClick = {
                    dropDownExpanded.value = true
                }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "More options")
                }
            }
            DropdownMenu(
                expanded = dropDownExpanded.value,
                onDismissRequest = { dropDownExpanded.value = false },
            ) {
                for (i in 1..6) {
                    DropdownMenuItem(
                        onClick = {
                            day.value = i
                            dropDownExpanded.value = false
                        }
                    ) {
                        Text(i.toDayOfTheWeekString(LocalContext.current))
                    }
                }

                DropdownMenuItem(
                    onClick = {
                        day.value = null
                        dropDownExpanded.value = false
                    }
                ) {
                    Text(stringResource(id = R.string.dont_assign))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TitleH4(R.string.meal, modifier = Modifier.padding(start = 8.dp))
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


