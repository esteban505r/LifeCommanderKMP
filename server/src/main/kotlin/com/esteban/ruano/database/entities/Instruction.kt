package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object Instructions : UUIDTable() {
    val stepNumber = integer("step_number")
    val description = text("description")
    val recipe = reference("recipe_id", Recipes, onDelete = ReferenceOption.CASCADE)
}

class Instruction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Instruction>(Instructions)
    var stepNumber by Instructions.stepNumber
    var description by Instructions.description
    var recipe by Recipe referencedOn Instructions.recipe
} 