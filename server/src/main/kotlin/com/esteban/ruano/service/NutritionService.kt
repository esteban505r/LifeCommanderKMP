package com.esteban.ruano.service

import fromDateToLong
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.database.entities.Recipes
import com.esteban.ruano.database.entities.Tasks
import com.esteban.ruano.database.models.MealTag
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.nutrition.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import parseDate
import java.util.UUID

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
            Recipe.find { (Recipes.user eq userId) and (Recipes.day eq day) and (Recipes.status eq Status.ACTIVE) }
                .toList().map { it.toDTO() }
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

}