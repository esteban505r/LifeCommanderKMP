package com.esteban.ruano.lifecommander.ui.state

import com.esteban.ruano.lifecommander.models.Recipe

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val daySelected: Int = 0
) 