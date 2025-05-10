package com.esteban.ruano.nutrition_presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core.utils.Constants.EMPTY_STRING
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.text.TitleH3
import com.esteban.ruano.core_ui.theme.Gray
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

    Column(
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        AppBar(
            state.recipe?.name ?: EMPTY_STRING,
            onClose = {
                onClose(false)
            },
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 16.dp).fillMaxWidth(),
        ) {
            IconButton(onClick = {
                userIntent(RecipeDetailIntent.EditRecipe)
            },
            ) {
                Icon(Icons.Default.Edit, contentDescription = "More options")
            }
            IconButton(onClick = {
                userIntent(RecipeDetailIntent.DeleteRecipe)
            },
            ) {
                Icon(Icons.Default.Delete, contentDescription = "More options")
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(text = stringResource(id = R.string.protein))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.recipe?.protein?.toString() ?: EMPTY_STRING,
            style = MaterialTheme.typography.body1,
            color = Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(text = stringResource(id = R.string.notes))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.recipe?.note ?: EMPTY_STRING,
            style = MaterialTheme.typography.body1,
            color = Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(text = stringResource(id = R.string.day))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.recipe?.day?.toDayOfTheWeekString(LocalContext.current) ?: stringResource(id = R.string.dont_assign),
            style = MaterialTheme.typography.body1,
            color = Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(text = stringResource(id = R.string.meal))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.recipe?.mealTag?.let { MealTag.valueOf(it).name } ?: stringResource(id = R.string.dont_assign),
            style = MaterialTheme.typography.body1,
            color = Gray
        )
    }
}


