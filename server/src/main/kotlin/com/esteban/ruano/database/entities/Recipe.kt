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
    val image = varchar("image", 255).nullable()
    val day = integer("day").nullable()
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val mealTag = enumerationByName("meal_tag", 20, MealTag::class).nullable()
}

class Recipe(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<Recipe>(Recipes)
    var name by Recipes.name
    var note by Recipes.note
    var protein by Recipes.protein
    var image by Recipes.image
    var user by User referencedOn Tasks.user
    var status by Tasks.status
    var day by Recipes.day
    var mealTag by Recipes.mealTag
}
