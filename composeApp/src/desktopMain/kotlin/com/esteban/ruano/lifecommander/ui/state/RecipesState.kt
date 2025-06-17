package com.esteban.ruano.lifecommander.ui.state

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.RecipeTrack

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val daySelected: Int = 0,
    val recipeTracks: List<RecipeTrack> = emptyList()
) 