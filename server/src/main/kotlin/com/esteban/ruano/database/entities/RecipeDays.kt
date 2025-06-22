package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object RecipeDays : UUIDTable() {
    val recipeId = reference("recipe_id", Recipes.id, onDelete = ReferenceOption.CASCADE)
    val day = integer("day") // 1-7 representing days of the week
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class RecipeDay(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RecipeDay>(RecipeDays)
    
    var recipe by Recipe referencedOn RecipeDays.recipeId
    var day by RecipeDays.day
    var user by User referencedOn RecipeDays.user
    var status by RecipeDays.status
} 