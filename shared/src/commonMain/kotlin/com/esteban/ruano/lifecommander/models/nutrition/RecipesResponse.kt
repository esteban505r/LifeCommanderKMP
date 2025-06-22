package com.esteban.ruano.lifecommander.models.nutrition

import com.esteban.ruano.lifecommander.models.Recipe

data class RecipesResponse(
    val recipes: List<Recipe>,
    val totalCount: Long
) 