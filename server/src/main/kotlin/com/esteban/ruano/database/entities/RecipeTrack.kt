package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object RecipeTracks : UUIDTable() {
    val recipeId = reference("recipe_id", Recipes.id, onDelete = ReferenceOption.CASCADE)
    val consumedDateTime = datetime("consumed_date_time")
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class RecipeTrack(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RecipeTrack>(RecipeTracks)

    var recipe by Recipe referencedOn RecipeTracks.recipeId
    var consumedDateTime by RecipeTracks.consumedDateTime
    var status by RecipeTracks.status
} 