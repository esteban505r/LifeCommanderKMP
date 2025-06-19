package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.text.TitleH3
import com.esteban.ruano.nutrition_presentation.intent.NutritionIntent
import com.esteban.ruano.nutrition_presentation.ui.composables.RecipeComposable
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.NutritionState

@Composable
fun NutritionScreen(
    onNavigateUp: () -> Unit,
    userIntent: (NutritionIntent) -> Unit,
    onRecipesClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    state: NutritionState
){

    Scaffold {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(it).padding(24.dp)
        ) {
            item {
                Text(
                    stringResource(id = R.string.nutrition),
                    style = MaterialTheme.typography.h2,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Card(
                    modifier = Modifier.clickable { onRecipesClick() }
                ) {
                   Box(
                          modifier = Modifier.fillMaxWidth()
                   ){
                       Text(text = "You have ${state.totalRecipes} recipes",
                           modifier = Modifier.align(Alignment.Center).padding(16.dp))
                   }
                }
            }
            item{
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Card(
                    modifier = Modifier.clickable { /* TODO: Navigate to meal tracking */ }
                ) {
                   Box(
                          modifier = Modifier.fillMaxWidth()
                   ){
                       Text(text = "Track Today's Meals",
                           modifier = Modifier.align(Alignment.Center).padding(16.dp))
                   }
                }
            }
            item {
                TitleH3(
                    text = "Today's recipes",
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            if(state.todayRecipes.isEmpty()){
                item{
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    ) {
                        Image(
                            painter = painterResource(state.emptyImageRes ?: R.drawable.empty_recipes),
                            contentDescription = "Recipe image",
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.empty_recipes),
                            style = MaterialTheme.typography.subtitle1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(400.dp)
                        )
                    }
                }
            }
            items(state.todayRecipes.size) { index ->
               RecipeComposable(
                   recipe = state.todayRecipes[index],
                   onClick = {
                       onRecipeClick(state.todayRecipes[index].id)
                   }
               )
            }
        }
    }
}