package com.esteban.ruano.nutrition_domain.use_cases


data class RecipeUseCases(
    val getAll: GetAllRecipes,
    val getByDay: GetRecipesByDay,
    val getRecipe: GetRecipe,
    val addRecipe: AddRecipe,
    val deleteRecipe: DeleteRecipe,
    val updateRecipe: UpdateRecipe
)