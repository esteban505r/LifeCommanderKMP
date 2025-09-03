package com.esteban.ruano.database.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

object Ingredients : UUIDTable() {
    val name = varchar("name", 255)
    val quantity = double("quantity")
    val unit = varchar("unit", 50)
    val recipe = reference("recipe_id", Recipes, onDelete = ReferenceOption.CASCADE)
}

class Ingredient(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Ingredient>(Ingredients)
    var name by Ingredients.name
    var quantity by Ingredients.quantity
    var unit by Ingredients.unit
    var recipe by Recipe referencedOn Ingredients.recipe
} 