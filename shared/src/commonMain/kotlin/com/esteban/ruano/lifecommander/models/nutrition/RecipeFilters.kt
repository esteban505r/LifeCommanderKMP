package com.esteban.ruano.lifecommander.models.nutrition

enum class RecipeSortOrder {
    NONE,
    ASCENDING,
    DESCENDING
}

enum class RecipeSortField {
    NAME,
    CALORIES,
    PROTEIN,
    CARBS,
    FAT,
    FIBER,
    SUGAR,
    SODIUM
}

data class RecipeFilters(
    val searchPattern: String? = null,
    val mealTypes: List<String>? = null,
    val days: List<String>? = null,
    val minCalories: Double? = null,
    val maxCalories: Double? = null,
    val minProtein: Double? = null,
    val maxProtein: Double? = null,
    val minCarbs: Double? = null,
    val maxCarbs: Double? = null,
    val minFat: Double? = null,
    val maxFat: Double? = null,
    val minFiber: Double? = null,
    val maxFiber: Double? = null,
    val minSugar: Double? = null,
    val maxSugar: Double? = null,
    val minSodium: Double? = null,
    val maxSodium: Double? = null,
    val sortField: RecipeSortField = RecipeSortField.NAME,
    val sortOrder: RecipeSortOrder = RecipeSortOrder.NONE,
) 