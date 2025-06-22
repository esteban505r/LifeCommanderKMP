package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.MealTag
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object Recipes : UUIDTable() {
    val name = varchar("name", 255)
    val note = text("note").nullable()
    val protein = double("protein").nullable()
    val calories = double("calories").nullable()
    val carbs = double("carbs").nullable()
    val fat = double("fat").nullable()
    val fiber = double("fiber").nullable()
    val sugar = double("sugar").nullable()

    val sodium = double("sodium").nullable()
    val image = varchar("image", 255).nullable()
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val mealTag = enumerationByName("meal_tag", 20, MealTag::class).nullable()
}

class Recipe(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<Recipe>(Recipes)
    var name by Recipes.name
    var note by Recipes.note
    var protein by Recipes.protein
    var calories by Recipes.calories
    var carbs by Recipes.carbs
    var fat by Recipes.fat
    var fiber by Recipes.fiber
    var sugar by Recipes.sugar
    var sodium by Recipes.sodium
    var image by Recipes.image
    var user by User referencedOn Recipes.user
    var status by Recipes.status
    var mealTag by Recipes.mealTag
    
    val recipeDays by RecipeDay referrersOn RecipeDays.recipeId
    val ingredients by Ingredient referrersOn Ingredients.recipe
    val instructions by Instruction referrersOn Instructions.recipe
}
