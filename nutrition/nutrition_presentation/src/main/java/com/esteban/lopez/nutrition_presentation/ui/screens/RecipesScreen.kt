package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.ToggleChipsButtons
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayNumber
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.nutrition_presentation.intent.RecipesIntent
import com.esteban.ruano.nutrition_presentation.ui.composables.RecipeComposable
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.RecipesState
import java.time.DayOfWeek

@Composable
fun RecipesScreen(
    onNavigateUp: () -> Unit,
    userIntent: (RecipesIntent) -> Unit,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit,
    state: RecipesState
){
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = {
                    onNewRecipe()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(id = R.string.new_recipe_title),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(24.dp)
        ) {
            item {
                Text(
                    stringResource(id = R.string.recipes),
                    style = MaterialTheme.typography.h2,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item{
                ToggleChipsButtons(
                    state.daySelected,
                    buttons = listOf(stringResource(R.string.all), stringResource(R.string.database)).plus(DayOfWeek.entries.toList().map {
                        it.value.toDayOfTheWeekString(LocalContext.current)
                    }),
                    onCheckedChange = {
                        when {
                            it == context.getString(R.string.all) -> {
                                userIntent(RecipesIntent.GetRecipes)
                            }
                            it == context.getString(R.string.database) -> {
                                userIntent(RecipesIntent.GetAllRecipes)
                            }
                            else -> {
                                userIntent(RecipesIntent.GetRecipesByDay(it.toDayNumber()))
                            }
                        }
                    })
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            if(state.recipes.isNotEmpty()){
                items(state.recipes.size) { index ->
                    RecipeComposable(
                        recipe = state.recipes[index],
                        showDay = state.daySelected == 0 || state.daySelected == -1,
                        onClick = {
                            onDetailRecipe(state.recipes[index].id)
                        }
                    )
                }
            }
            else{
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(
                            text = "No recipes found",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .align(
                                    alignment = androidx.compose.ui.Alignment.Center
                                )
                                .padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}