package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.MuscleGroup
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object Exercises : UUIDTable() {

    val name = varchar("name", 50)
    val description = varchar("description", 255)
    val restSecs = integer("rest_secs")
    val baseSets = integer("base_sets")
    val baseReps = integer("base_reps")
    val muscleGroup = enumerationByName("muscle_group", 50, MuscleGroup::class).default(MuscleGroup.FULL_BODY)
    val resource = optReference("resource_id", Resources, ReferenceOption.CASCADE)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Exercise(id: EntityID<UUID>) :  UUIDEntity(id) {
    companion object : UUIDEntityClass<Exercise>(Exercises)

    var name by Exercises.name
    var description by Exercises.description
    var restSecs by Exercises.restSecs
    var baseSets by Exercises.baseSets
    var baseReps by Exercises.baseReps
    var muscleGroup by Exercises.muscleGroup
    var resource by Resource optionalReferencedOn Exercises.resource
    var user by User referencedOn Exercises.user
    var status by Exercises.status
}
