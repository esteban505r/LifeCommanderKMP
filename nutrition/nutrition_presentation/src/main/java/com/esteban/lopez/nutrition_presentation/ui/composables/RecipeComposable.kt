package com.esteban.ruano.nutrition_presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
        modifier = Modifier.padding(bottom = 16.dp),
        onClick = onClick
    ) {
        ListTile(
            modifier = Modifier.padding(0.dp),
            title = recipe.name,
            subtitle = "Protein: ${recipe.protein} gr",
            contentWeight = 2f,
            suffix = {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(end = 8.dp).weight(0.5f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        recipe.mealTag ?: stringResource(R.string.no_meal),
                        style = MaterialTheme.typography.body2
                    )
                    if (showDay) {
                        Text(
                            recipe.day?.toDayOfTheWeekString(LocalContext.current)?: stringResource(com.esteban.ruano.core_ui.R.string.dont_assign).uppercase(),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            },
            prefix = {
                Card(
                    elevation = 0.dp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp, end = 16.dp),
                    backgroundColor = LightGray4
                ) {
                    Icon(
                        Icons.Default.Fastfood,
                        contentDescription = "Food",
                        tint = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
        )
    }
   }