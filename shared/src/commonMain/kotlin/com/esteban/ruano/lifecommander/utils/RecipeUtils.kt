package com.esteban.ruano.lifecommander.utils

import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters

fun RecipeFilters.buildParametersString(): String? {
    val params = mutableListOf<String>()
    
    searchPattern?.let { params.add("search=$it") }
    mealTypes?.forEach { params.add("mealType=$it") }
    days?.forEach { params.add("day=$it") }
    minCalories?.let { params.add("minCalories=$it") }
    maxCalories?.let { params.add("maxCalories=$it") }
    minProtein?.let { params.add("minProtein=$it") }
    maxProtein?.let { params.add("maxProtein=$it") }
    minCarbs?.let { params.add("minCarbs=$it") }
    maxCarbs?.let { params.add("maxCarbs=$it") }
    minFat?.let { params.add("minFat=$it") }
    maxFat?.let { params.add("maxFat=$it") }
    minFiber?.let { params.add("minFiber=$it") }
    maxFiber?.let { params.add("maxFiber=$it") }
    minSugar?.let { params.add("minSugar=$it") }
    maxSugar?.let { params.add("maxSugar=$it") }
    minSodium?.let { params.add("minSodium=$it") }
    maxSodium?.let { params.add("maxSodium=$it") }
    sortField.takeIf { it != com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.NAME }?.let { params.add("sortField=$it") }
    sortOrder.takeIf { it != com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.NONE }?.let { params.add("sortOrder=$it") }
    
    return if (params.isNotEmpty()) params.joinToString("&") else null
} 