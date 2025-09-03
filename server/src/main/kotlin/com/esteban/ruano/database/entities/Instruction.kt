package com.esteban.ruano.database.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

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