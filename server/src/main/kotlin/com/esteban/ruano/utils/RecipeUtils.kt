package com.esteban.ruano.utils

import io.ktor.server.routing.*
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField
import com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder

fun RoutingCall.gatherRecipeFilters(): RecipeFilters {
    val searchPattern = this.parameters["search"]
    val mealTypes = this.parameters.getAll("mealType")
    val days = this.parameters.getAll("day")
    val minCalories = this.parameters["minCalories"]?.toDoubleOrNull()
    val maxCalories = this.parameters["maxCalories"]?.toDoubleOrNull()
    val minProtein = this.parameters["minProtein"]?.toDoubleOrNull()
    val maxProtein = this.parameters["maxProtein"]?.toDoubleOrNull()
    val minCarbs = this.parameters["minCarbs"]?.toDoubleOrNull()
    val maxCarbs = this.parameters["maxCarbs"]?.toDoubleOrNull()
    val minFat = this.parameters["minFat"]?.toDoubleOrNull()
    val maxFat = this.parameters["maxFat"]?.toDoubleOrNull()
    val minFiber = this.parameters["minFiber"]?.toDoubleOrNull()
    val maxFiber = this.parameters["maxFiber"]?.toDoubleOrNull()
    val minSugar = this.parameters["minSugar"]?.toDoubleOrNull()
    val maxSugar = this.parameters["maxSugar"]?.toDoubleOrNull()
    val minSodium = this.parameters["minSodium"]?.toDoubleOrNull()
    val maxSodium = this.parameters["maxSodium"]?.toDoubleOrNull()
    
    val sortField = this.parameters["sortField"]?.let {
        try {
            RecipeSortField.valueOf(it)
        } catch (e: IllegalArgumentException) {
            RecipeSortField.NAME
        }
    } ?: RecipeSortField.NAME
    
    val sortOrder = this.parameters["sortOrder"]?.let {
        try {
            RecipeSortOrder.valueOf(it)
        } catch (e: IllegalArgumentException) {
            RecipeSortOrder.NONE
        }
    } ?: RecipeSortOrder.NONE

    return RecipeFilters(
        searchPattern = searchPattern,
        mealTypes = mealTypes,
        days = days,
        minCalories = minCalories,
        maxCalories = maxCalories,
        minProtein = minProtein,
        maxProtein = maxProtein,
        minCarbs = minCarbs,
        maxCarbs = maxCarbs,
        minFat = minFat,
        maxFat = maxFat,
        minFiber = minFiber,
        maxFiber = maxFiber,
        minSugar = minSugar,
        maxSugar = maxSugar,
        minSodium = minSodium,
        maxSodium = maxSodium,
        sortField = sortField,
        sortOrder = sortOrder
    )
} 