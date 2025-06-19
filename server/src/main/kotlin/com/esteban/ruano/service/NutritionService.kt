package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.database.entities.RecipeTrack
import com.esteban.ruano.database.entities.RecipeTracks
import com.esteban.ruano.database.entities.Recipes
import com.esteban.ruano.database.models.MealTag
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.nutrition.*
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime as toLocalDateTimeUI
import com.esteban.ruano.utils.fromDateToLong
import com.esteban.ruano.utils.parseDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime

class NutritionService() : BaseService() {

    fun getDashboard(userId: Int,date:String): NutritionDashboardDTO {
        return transaction {
            val recipes = Recipe.find { (Recipes.user eq userId )
                .and (Recipes.status eq Status.ACTIVE)
            }.toList().map { it.toDTO() }
            val totalRecipes = recipes.size
            NutritionDashboardDTO(totalRecipes, recipes.filter {
                it.day == parseDate(date).dayOfWeek.value
            })
        }
    }

    fun getRecipesByDay(userId: Int, day: Int): List<RecipeDTO> {
        return transaction {
            val recipes = Recipe.find { (Recipes.user eq userId) and (Recipes.day eq day) and (Recipes.status eq Status.ACTIVE) }
                .toList().map { it.toDTO() }
            
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
                .firstOrNull()?.toDTO()
        }
    }

    fun createRecipe(userId: Int, recipe: CreateRecipeDTO): UUID? {
        return transaction {
            val id = Recipes.insertOperation(userId, recipe.createdAt?.fromDateToLong()) {
                insert {
                    it[name] = recipe.name
                    it[protein] = recipe.protein
                    it[image] = recipe.image
                    it[day] = recipe.day
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
            id
        }
    }

    fun updateRecipe(userId: Int, id: UUID, recipe: UpdateRecipeDTO): Boolean {
        return transaction {
            val updatedRow = Recipes.updateOperation(userId, recipe.updatedAt?.fromDateToLong()) {
                val updatedRows = update({ (Recipes.id eq id) }) { row ->
                    recipe.name.let { row[name] = it }
                    recipe.protein?.let { row[protein] = it }
                    recipe.image?.let { row[image] = it }
                    row[day] = recipe.day
                    recipe.note?.let { row[note] = it }
                    recipe.mealTag?.let { row[mealTag] = try {
                        MealTag.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                    }
                }
                if (updatedRows > 0) id else null
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
            deletedRow != null
        }
    }

    fun fetchAllRecipes(userId: Int, filter: String, limit: Int, offset: Long): List<RecipeDTO> {
        return transaction {
            Recipe.find{
                (Recipes.user eq userId) and (Recipes.name.lowerCase() like "%${filter.lowercase()}%" and (Recipes.status eq Status.ACTIVE))
            }.limit(limit, offset).toList().map { it.toDTO() }
        }
    }

    fun getRecipesNotAssignedToDay(userId: Int, filter: String, limit: Int, offset: Long): List<RecipeDTO> {
        return transaction {
            Recipe.find{
                (Recipes.user eq userId) and 
                (Recipes.day.isNull()) and 
                (Recipes.name.lowerCase() like "%${filter.lowercase()}%") and 
                (Recipes.status eq Status.ACTIVE)
            }.limit(limit, offset).toList().map { it.toDTO() }
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
        val startDateTime = startDateStr.toLocalDateTimeUI()
        val endDateTime = endDateStr.toLocalDateTimeUI()
        try{
            return transaction {
                val activeRecipeIds = Recipe
                    .find { (Recipes.user eq userId) and (Recipes.status eq Status.ACTIVE) }
                    .map { it.id }

                RecipeTrack.find {
                    (RecipeTracks.recipeId inList activeRecipeIds) and
                            (RecipeTracks.consumedDateTime greaterEq startDateTime) and
                            (RecipeTracks.consumedDateTime lessEq endDateTime) and
                            (RecipeTracks.status eq Status.ACTIVE)
                }.map { it.toDTO() }
            }
        }
        catch (e: Exception) {
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
}