package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.converters.toTrackDTO
import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.database.entities.RecipeTrack
import com.esteban.ruano.database.entities.RecipeTracks
import com.esteban.ruano.database.entities.Recipes
import com.esteban.ruano.database.entities.RecipeDays
import com.esteban.ruano.database.entities.RecipeDay
import com.esteban.ruano.database.entities.Ingredients
import com.esteban.ruano.database.entities.Instructions
import com.esteban.ruano.database.models.MealTag
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.nutrition.*
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUI
import com.esteban.ruano.utils.fromDateToLong
import com.esteban.ruano.utils.parseDate
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import com.esteban.ruano.models.nutrition.RecipeTrackDTO
import com.esteban.ruano.models.nutrition.UpdateRecipeDTO
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.like
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.datetime.date
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class NutritionService() : BaseService() {

    fun getDashboard(userId: Int, date: String): NutritionDashboardDTO {
        return transaction {
            val recipes = Recipe.find {
                (Recipes.user eq userId)
                    .and(Recipes.status eq Status.ACTIVE)
            }.with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions).map { it.toDTO() }
            val totalRecipes = recipes.size
            val currentDay = parseDate(date).dayOfWeek.ordinal
            NutritionDashboardDTO(totalRecipes, recipes.filter {
                it.days?.contains(currentDay) == true
            })
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getRecipesByDay(
        userId: Int,
        day: Int,
        filter: String = "",
        limit: Int = 50,
        offset: Long = 0,
        sortBy: String = "name",
        sortOrder: String = "asc",
        mealTagFilter: String? = null,
        nutritionFilters: Map<String, Pair<Double?, Double?>> = emptyMap()
    ): List<RecipeDTO> {
        return transaction {
            // Get recipes that are assigned to the specified day
            val recipeIds = RecipeDay.find {
                (RecipeDays.user eq userId) and
                        (RecipeDays.day eq day) and
                        (RecipeDays.status eq Status.ACTIVE)
            }.map { it.recipe.id }

            // Build the complete condition
            var condition = (Recipes.id inList recipeIds) and
                    (Recipes.status eq Status.ACTIVE) and
                    (Recipes.name.lowerCase() like "%${filter.lowercase()}%")

            // Apply meal tag filter
            mealTagFilter?.let { tag ->
                try {
                    val mealTag = MealTag.valueOf(tag)
                    condition = condition and (Recipes.mealTag eq mealTag)
                } catch (e: IllegalArgumentException) {
                    // Invalid meal tag, ignore filter
                }
            }

            // Apply nutrition filters
            nutritionFilters.forEach { (type, range) ->
                val (minValue, maxValue) = range
                when (type.lowercase()) {
                    "calories" -> {
                        minValue?.let { condition = condition and (Recipes.calories greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.calories lessEq it) }
                    }

                    "protein" -> {
                        minValue?.let { condition = condition and (Recipes.protein greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.protein lessEq it) }
                    }

                    "carbs" -> {
                        minValue?.let { condition = condition and (Recipes.carbs greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.carbs lessEq it) }
                    }

                    "fat" -> {
                        minValue?.let { condition = condition and (Recipes.fat greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.fat lessEq it) }
                    }

                    "fiber" -> {
                        minValue?.let { condition = condition and (Recipes.fiber greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.fiber lessEq it) }
                    }

                    "sugar" -> {
                        minValue?.let { condition = condition and (Recipes.sugar greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.sugar lessEq it) }
                    }
                }
            }

            // Create the query with the complete condition
            var query = Recipe.find(condition)

            // Apply sorting
            val sortedQuery = when (sortBy.lowercase()) {
                "name" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.name to SortOrder.DESC) else query.orderBy(
                    Recipes.name to SortOrder.ASC
                )

                "calories" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.calories to SortOrder.DESC) else query.orderBy(
                    Recipes.calories to SortOrder.ASC
                )

                "protein" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.protein to SortOrder.DESC) else query.orderBy(
                    Recipes.protein to SortOrder.ASC
                )

                "carbs" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.carbs to SortOrder.DESC) else query.orderBy(
                    Recipes.carbs to SortOrder.ASC
                )

                "fat" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.fat to SortOrder.DESC) else query.orderBy(
                    Recipes.fat to SortOrder.ASC
                )

                "fiber" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.fiber to SortOrder.DESC) else query.orderBy(
                    Recipes.fiber to SortOrder.ASC
                )

                "sugar" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.sugar to SortOrder.DESC) else query.orderBy(
                    Recipes.sugar to SortOrder.ASC
                )

                else -> query.orderBy(Recipes.name to SortOrder.ASC)
            }

            val recipes = sortedQuery.limit(limit).offset(offset)
                .with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions).map { it.toDTO() }

            // Add consumption status to each recipe
            recipes.map { recipe ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startOfDay = today.atTime(0, 0)
                val endOfDay = today.atTime(23, 59)

                val recentTrack = RecipeTrack.find {
                    (RecipeTracks.recipeId eq UUID.fromString(recipe.id)) and
                            (RecipeTracks.consumedDateTime greaterEq startOfDay) and
                            (RecipeTracks.consumedDateTime lessEq endOfDay) and
                            (RecipeTracks.status eq Status.ACTIVE)
                }.firstOrNull()

                recipe.copy(
                    consumed = recentTrack != null,
                    consumedDateTime = recentTrack?.consumedDateTime?.toString()
                )
            }
        }
    }

    fun getRecipesByIdAndUserId(userId: Int, id: UUID): RecipeDTO? {
        return transaction {
            Recipe.find { (Recipes.id eq id) and (Recipes.user eq userId) and (Recipes.status eq Status.ACTIVE) }
                .with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions)
                .firstOrNull()
                ?.toDTO()
        }
    }

    fun createRecipe(userId: Int, recipe: CreateRecipeDTO): UUID? {
        return transaction {
            val recipeId = Recipes.insertOperation(userId, recipe.createdAt?.fromDateToLong()) {
                insert {
                    it[name] = recipe.name
                    it[protein] = recipe.protein
                    it[calories] = recipe.calories
                    it[carbs] = recipe.carbs
                    it[fat] = recipe.fat
                    it[fiber] = recipe.fiber
                    it[sodium] = recipe.sodium
                    it[sugar] = recipe.sugar
                    it[image] = recipe.image
                    it[note] = recipe.note
                    it[user] = userId
                    it[mealTag] = recipe.mealTag?.let {
                        try {
                            MealTag.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }

            recipeId?.let { id ->
                // Create Ingredient entries
                recipe.ingredients.forEach { ingredient ->
                    Ingredients.insert {
                        it[name] = ingredient.name
                        it[quantity] = ingredient.quantity
                        it[unit] = ingredient.unit
                        it[this.recipe] = id
                    }
                }

                // Create Instruction entries
                recipe.instructions.forEach { instruction ->
                    Instructions.insert {
                        it[stepNumber] = instruction.stepNumber
                        it[description] = instruction.description
                        it[this.recipe] = id
                    }
                }

                // Create RecipeDay entries
                recipe.days.forEach { day ->
                    RecipeDays.insertOperation(userId, recipe.createdAt?.fromDateToLong()) {
                        val id = insert {
                            it[this.recipeId] = id
                            it[this.day] = day
                            it[user] = userId
                            it[status] = Status.ACTIVE
                        }
                        id.resultedValues?.firstOrNull()?.getOrNull(RecipeDays.id)?.value
                    }
                }
            }

            recipeId
        }
    }

    fun updateRecipe(userId: Int, id: UUID, recipe: UpdateRecipeDTO): Boolean {
        return transaction {
            val updatedRow = Recipes.updateOperation(userId, recipe.updatedAt?.fromDateToLong()) {
                val updatedRows = update({ (Recipes.id eq id) }) { row ->
                    recipe.name.let { row[name] = it }
                    recipe.protein?.let { row[protein] = it }
                    recipe.calories?.let { row[calories] = it }
                    recipe.carbs?.let { row[carbs] = it }
                    recipe.fat?.let { row[fat] = it }
                    recipe.fiber?.let { row[fiber] = it }
                    recipe.sugar?.let { row[sugar] = it }
                    recipe.image?.let { row[image] = it }
                    recipe.note?.let { row[note] = it }
                    recipe.mealTag?.let {
                        row[mealTag] = try {
                            MealTag.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                }
                if (updatedRows > 0) id else null
            }

            if (updatedRow != null) {
                // Clear old ingredients and instructions
                Ingredients.deleteWhere { Ingredients.recipe eq id }
                Instructions.deleteWhere { Instructions.recipe eq id }

                // Add new ingredients
                recipe.ingredients.forEach { ingredient ->
                    Ingredients.insert {
                        it[name] = ingredient.name
                        it[quantity] = ingredient.quantity
                        it[unit] = ingredient.unit
                        it[this.recipe] = id
                    }
                }

                // Add new instructions
                recipe.instructions.forEach { instruction ->
                    Instructions.insert {
                        it[stepNumber] = instruction.stepNumber
                        it[description] = instruction.description
                        it[this.recipe] = id
                    }
                }

                // Update RecipeDay entries
                // First, mark all existing days as deleted
                RecipeDays.update({ (RecipeDays.recipeId eq id) }) {
                    it[status] = Status.DELETED
                }

                // Then create new entries for the specified days
                recipe.days.forEach { day ->
                    RecipeDays.insertOperation(userId, recipe.updatedAt?.fromDateToLong()) {
                        val id = insert {
                            it[this.recipeId] = id
                            it[this.day] = day
                            it[user] = userId
                            it[status] = Status.ACTIVE
                        }
                        id.resultedValues?.firstOrNull()?.getOrNull(RecipeDays.id)?.value
                    }
                }
            }

            updatedRow != null
        }
    }

    fun deleteRecipe(userId: Int, id: UUID): Boolean {
        return transaction {
            val deletedRow = Recipes.deleteOperation(userId) {
                val updatedRows = Recipes.update({ (Recipes.id eq id) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) id else null
            }

            // Also mark all associated RecipeDay entries as deleted
            if (deletedRow != null) {
                RecipeDays.update({ (RecipeDays.recipeId eq id) }) {
                    it[status] = Status.DELETED
                }
            }

            deletedRow != null
        }
    }

    fun fetchAllRecipes(
        userId: Int,
        filter: String,
        limit: Int,
        offset: Long,
        sortBy: String = "name",
        sortOrder: String = "asc",
        mealTagFilter: String? = null,
        nutritionFilters: Map<String, Pair<Double?, Double?>> = emptyMap()
    ): List<RecipeDTO> {
        return transaction {
            // Build the complete condition
            var condition = (Recipes.user eq userId) and
                    (Recipes.name.lowerCase() like "%${filter.lowercase()}%") and
                    (Recipes.status eq Status.ACTIVE)

            // Apply meal tag filter
            mealTagFilter?.let { tag ->
                try {
                    val mealTag = MealTag.valueOf(tag)
                    condition = condition and (Recipes.mealTag eq mealTag)
                } catch (e: IllegalArgumentException) {
                    // Invalid meal tag, ignore filter
                }
            }

            // Apply nutrition filters
            nutritionFilters.forEach { (type, range) ->
                val (minValue, maxValue) = range
                when (type.lowercase()) {
                    "calories" -> {
                        minValue?.let { condition = condition and (Recipes.calories greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.calories lessEq it) }
                    }

                    "protein" -> {
                        minValue?.let { condition = condition and (Recipes.protein greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.protein lessEq it) }
                    }

                    "carbs" -> {
                        minValue?.let { condition = condition and (Recipes.carbs greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.carbs lessEq it) }
                    }

                    "fat" -> {
                        minValue?.let { condition = condition and (Recipes.fat greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.fat lessEq it) }
                    }

                    "fiber" -> {
                        minValue?.let { condition = condition and (Recipes.fiber greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.fiber lessEq it) }
                    }

                    "sugar" -> {
                        minValue?.let { condition = condition and (Recipes.sugar greaterEq it) }
                        maxValue?.let { condition = condition and (Recipes.sugar lessEq it) }
                    }
                }
            }

            // Create the query with the complete condition
            var query = Recipe.find(condition)

            // Apply sorting
            val sortedQuery = when (sortBy.lowercase()) {
                "name" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.name to SortOrder.DESC) else query.orderBy(
                    Recipes.name to SortOrder.ASC
                )

                "calories" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.calories to SortOrder.DESC) else query.orderBy(
                    Recipes.calories to SortOrder.ASC
                )

                "protein" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.protein to SortOrder.DESC) else query.orderBy(
                    Recipes.protein to SortOrder.ASC
                )

                "carbs" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.carbs to SortOrder.DESC) else query.orderBy(
                    Recipes.carbs to SortOrder.ASC
                )

                "fat" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.fat to SortOrder.DESC) else query.orderBy(
                    Recipes.fat to SortOrder.ASC
                )

                "fiber" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.fiber to SortOrder.DESC) else query.orderBy(
                    Recipes.fiber to SortOrder.ASC
                )

                "sugar" -> if (sortOrder.lowercase() == "desc") query.orderBy(Recipes.sugar to SortOrder.DESC) else query.orderBy(
                    Recipes.sugar to SortOrder.ASC
                )

                else -> query.orderBy(Recipes.name to SortOrder.ASC)
            }

            sortedQuery.limit(limit).offset(offset)
                .with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions)
                .map { it.toDTO() }
        }
    }

    fun getRecipesNotAssignedToDay(userId: Int, filter: String, limit: Int, offset: Long): List<RecipeDTO> {
        return transaction {
            // Get all recipe IDs that have no active day assignments
            val recipesWithDays = RecipeDay.find {
                (RecipeDays.user eq userId) and
                        (RecipeDays.status eq Status.ACTIVE)
            }.map { it.recipe.id }.toSet()

            Recipe.find {
                (Recipes.user eq userId) and
                        (Recipes.id notInList recipesWithDays.toList()) and
                        (Recipes.name.lowerCase() like "%${filter.lowercase()}%") and
                        (Recipes.status eq Status.ACTIVE)
            }.limit(limit).offset(offset)
                .with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions)
                .map { it.toDTO() }
        }
    }

    // Recipe Tracking Methods
    fun trackRecipeConsumption(userId: Int, recipeTrack: CreateRecipeTrackDTO): UUID? {
        return transaction {
            // Verify the recipe belongs to the user
            val recipe = Recipe.find {
                (Recipes.id eq UUID.fromString(recipeTrack.recipeId)) and
                        (Recipes.user eq userId) and
                        (Recipes.status eq Status.ACTIVE)
            }.firstOrNull()

            if (recipe != null) {
                val id = RecipeTracks.insertOperation(userId, recipeTrack.consumedDateTime.fromDateToLong()) {
                    insert {
                        it[recipeId] = UUID.fromString(recipeTrack.recipeId)
                        it[consumedDateTime] = recipeTrack.consumedDateTime.toLocalDateTimeUI()
                        it[status] = Status.ACTIVE
                        it[skipped] = recipeTrack.skipped
                        it[alternativeRecipeId] = recipeTrack.alternativeRecipeId?.let { UUID.fromString(it) }
                        it[alternativeMealName] = recipeTrack.alternativeMealName
                        it[protein] = recipeTrack.alternativeNutrients?.protein
                        it[calories] = recipeTrack.alternativeNutrients?.calories
                        it[carbs] = recipeTrack.alternativeNutrients?.carbs
                        it[fat] = recipeTrack.alternativeNutrients?.fat
                        it[fiber] = recipeTrack.alternativeNutrients?.fiber
                        it[sodium] = recipeTrack.alternativeNutrients?.sodium
                        it[sugar] = recipeTrack.alternativeNutrients?.sugar
                    }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
                }
                id
            } else {
                null
            }
        }
    }

    fun getRecipeTracksByDateRange(
        userId: Int,
        startDateStr: String,
        endDateStr: String
    ): List<RecipeTrackDTO> {
        val startDateTime = startDateStr.toLocalDate()
        val endDateTime = endDateStr.toLocalDate()
        try {
            return transaction {
                val activeRecipeIds = Recipe
                    .find { (Recipes.user eq userId) and (Recipes.status eq Status.ACTIVE) }
                    .map { it.id }

                RecipeTrack.find {
                    (RecipeTracks.recipeId inList activeRecipeIds) and
                            (RecipeTracks.consumedDateTime.date() greaterEq startDateTime) and
                            (RecipeTracks.consumedDateTime.date() lessEq endDateTime) and
                            (RecipeTracks.status eq Status.ACTIVE)
                }.with(RecipeTrack::recipe, Recipe::ingredients, Recipe::instructions).map {
                    it.toTrackDTO()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun getRecipeTracksByRecipe(userId: Int, recipeId: String): List<RecipeTrackDTO> {
        return transaction {
            RecipeTrack.find {
                (RecipeTracks.recipeId eq UUID.fromString(recipeId)) and
                        (RecipeTracks.status eq Status.ACTIVE)
            }.toList().map { it.toDTO() }
        }
    }

    fun deleteRecipeTrack(userId: Int, trackId: String): Boolean {
        return transaction {
            val deletedRow = RecipeTracks.deleteOperation(userId) {
                val updatedRows = RecipeTracks.update({ (RecipeTracks.id eq UUID.fromString(trackId)) }) {
                    it[status] = Status.DELETED
                }
                if (updatedRows > 0) {
                    UUID.fromString(trackId)
                } else {
                    null
                }
            }
            deletedRow != null
        }
    }

    fun getRecipeTracksByDate(userId: Int, date: String): List<RecipeTrackDTO> {
        val localDate = date.toLocalDate()
        return transaction {
            RecipeTrack.find {
                val recipeIds = Recipe.find { Recipes.user eq userId }.map { it.id }
                (RecipeTracks.recipeId inList recipeIds) and
                        (RecipeTracks.consumedDateTime.date() eq localDate) and
                        (RecipeTracks.status eq Status.ACTIVE)
            }
                .with(RecipeTrack::recipe, Recipe::ingredients, Recipe::instructions)
                .map { track ->
                    RecipeTrackDTO(
                        id = track.id.value.toString(),
                        recipe = track.recipe.toDTO(),
                        skipped = track.skipped,
                        consumedDateTime = track.consumedDateTime?.toString(),
                    )
                }
        }
    }

    fun getRecipesWithFilters(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters = com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters()
    ): com.esteban.ruano.models.nutrition.RecipesResponseDTO {
        return transaction {
            // Build the complete condition
            var condition = (Recipes.user eq userId) and (Recipes.status eq Status.ACTIVE)

            // Apply search pattern
            filters.searchPattern?.let { pattern ->
                condition = condition and (Recipes.name.lowerCase() like "%${pattern.lowercase()}%")
            }

            // Apply meal type filters
            filters.mealTypes?.let { mealTypes ->
                val mealTags = mealTypes.mapNotNull { type ->
                    try {
                        MealTag.valueOf(type)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                if (mealTags.isNotEmpty()) {
                    condition = condition and (Recipes.mealTag inList mealTags)
                }
            }

            // Apply day filters
            filters.days?.let { days ->
                val dayNumbers = days.mapNotNull { it.toIntOrNull() }
                if (dayNumbers.isNotEmpty()) {
                    val recipeIdsForDays = RecipeDay.find {
                        (RecipeDays.user eq userId) and
                                (RecipeDays.day inList dayNumbers) and
                                (RecipeDays.status eq Status.ACTIVE)
                    }.map { it.recipe.id }
                    condition = condition and (Recipes.id inList recipeIdsForDays)
                }
            }

            // Apply nutrition filters
            filters.minCalories?.let { condition = condition and (Recipes.calories greaterEq it) }
            filters.maxCalories?.let { condition = condition and (Recipes.calories lessEq it) }
            filters.minProtein?.let { condition = condition and (Recipes.protein greaterEq it) }
            filters.maxProtein?.let { condition = condition and (Recipes.protein lessEq it) }
            filters.minCarbs?.let { condition = condition and (Recipes.carbs greaterEq it) }
            filters.maxCarbs?.let { condition = condition and (Recipes.carbs lessEq it) }
            filters.minFat?.let { condition = condition and (Recipes.fat greaterEq it) }
            filters.maxFat?.let { condition = condition and (Recipes.fat lessEq it) }
            filters.minFiber?.let { condition = condition and (Recipes.fiber greaterEq it) }
            filters.maxFiber?.let { condition = condition and (Recipes.fiber lessEq it) }
            filters.minSugar?.let { condition = condition and (Recipes.sugar greaterEq it) }
            filters.maxSugar?.let { condition = condition and (Recipes.sugar lessEq it) }
            filters.minSodium?.let { condition = condition and (Recipes.sodium greaterEq it) }
            filters.maxSodium?.let { condition = condition and (Recipes.sodium lessEq it) }

            // Create the query with the complete condition
            var query = Recipe.find(condition)

            // Apply sorting
            val sortOrder = when (filters.sortOrder) {
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.ASCENDING -> SortOrder.ASC
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.DESCENDING -> SortOrder.DESC
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.NONE -> SortOrder.ASC
            }

            val sortedQuery = when (filters.sortField) {
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.NAME -> query.orderBy(Recipes.name to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.CALORIES -> query.orderBy(Recipes.calories to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.PROTEIN -> query.orderBy(Recipes.protein to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.CARBS -> query.orderBy(Recipes.carbs to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.FAT -> query.orderBy(Recipes.fat to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.FIBER -> query.orderBy(Recipes.fiber to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.SUGAR -> query.orderBy(Recipes.sugar to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.SODIUM -> query.orderBy(Recipes.sodium to sortOrder)
            }

            val totalCount = sortedQuery.count()
            val recipes = sortedQuery.limit(limit).offset(offset.toLong())
                .with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions)
                .map { it.toDTO() }

            com.esteban.ruano.models.nutrition.RecipesResponseDTO(
                recipes = recipes,
                totalCount = totalCount
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getRecipesByDayWithFilters(
        userId: Int,
        day: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters = com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters()
    ): com.esteban.ruano.models.nutrition.RecipesResponseDTO {
        return transaction {
            // Get recipes that are assigned to the specified day
            val recipeIds = RecipeDay.find {
                (RecipeDays.user eq userId) and
                        (RecipeDays.day eq day) and
                        (RecipeDays.status eq Status.ACTIVE)
            }.map { it.recipe.id }

            // Build the complete condition
            var condition = (Recipes.id inList recipeIds) and (Recipes.status eq Status.ACTIVE)

            // Apply search pattern
            filters.searchPattern?.let { pattern ->
                condition = condition and (Recipes.name.lowerCase() like "%${pattern.lowercase()}%")
            }

            // Apply meal type filters
            filters.mealTypes?.let { mealTypes ->
                val mealTags = mealTypes.mapNotNull { type ->
                    try {
                        MealTag.valueOf(type)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                if (mealTags.isNotEmpty()) {
                    condition = condition and (Recipes.mealTag inList mealTags)
                }
            }

            // Apply nutrition filters
            filters.minCalories?.let { condition = condition and (Recipes.calories greaterEq it) }
            filters.maxCalories?.let { condition = condition and (Recipes.calories lessEq it) }
            filters.minProtein?.let { condition = condition and (Recipes.protein greaterEq it) }
            filters.maxProtein?.let { condition = condition and (Recipes.protein lessEq it) }
            filters.minCarbs?.let { condition = condition and (Recipes.carbs greaterEq it) }
            filters.maxCarbs?.let { condition = condition and (Recipes.carbs lessEq it) }
            filters.minFat?.let { condition = condition and (Recipes.fat greaterEq it) }
            filters.maxFat?.let { condition = condition and (Recipes.fat lessEq it) }
            filters.minFiber?.let { condition = condition and (Recipes.fiber greaterEq it) }
            filters.maxFiber?.let { condition = condition and (Recipes.fiber lessEq it) }
            filters.minSugar?.let { condition = condition and (Recipes.sugar greaterEq it) }
            filters.maxSugar?.let { condition = condition and (Recipes.sugar lessEq it) }
            filters.minSodium?.let { condition = condition and (Recipes.sodium greaterEq it) }
            filters.maxSodium?.let { condition = condition and (Recipes.sodium lessEq it) }

            // Create the query with the complete condition
            var query = Recipe.find(condition)

            // Apply sorting
            val sortOrder = when (filters.sortOrder) {
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.ASCENDING -> SortOrder.ASC
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.DESCENDING -> SortOrder.DESC
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortOrder.NONE -> SortOrder.ASC
            }

            val sortedQuery = when (filters.sortField) {
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.NAME -> query.orderBy(Recipes.name to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.CALORIES -> query.orderBy(Recipes.calories to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.PROTEIN -> query.orderBy(Recipes.protein to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.CARBS -> query.orderBy(Recipes.carbs to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.FAT -> query.orderBy(Recipes.fat to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.FIBER -> query.orderBy(Recipes.fiber to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.SUGAR -> query.orderBy(Recipes.sugar to sortOrder)
                com.esteban.ruano.lifecommander.models.nutrition.RecipeSortField.SODIUM -> query.orderBy(Recipes.sodium to sortOrder)
            }

            val totalCount = sortedQuery.count()
            val recipes = sortedQuery.limit(limit).offset(offset.toLong())
                .with(Recipe::recipeDays, Recipe::ingredients, Recipe::instructions)
                .map { it.toDTO() }

            // Add consumption status to each recipe
            val recipesWithConsumption = recipes.map { recipe ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startOfDay = today.atTime(0, 0)
                val endOfDay = today.atTime(23, 59)

                val recentTrack = RecipeTrack.find {
                    (RecipeTracks.recipeId eq UUID.fromString(recipe.id)) and
                            (RecipeTracks.consumedDateTime greaterEq startOfDay) and
                            (RecipeTracks.consumedDateTime lessEq endOfDay) and
                            (RecipeTracks.status eq Status.ACTIVE)
                }.firstOrNull()

                recipe.copy(
                    consumed = recentTrack != null,
                    consumedDateTime = recentTrack?.consumedDateTime?.toString()
                )
            }

            com.esteban.ruano.models.nutrition.RecipesResponseDTO(
                recipes = recipesWithConsumption,
                totalCount = totalCount
            )
        }
    }
}