package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Equipments : IntIdTable() {

    val name = varchar("name", 50)
    val resource = reference("resource", Resources, ReferenceOption.CASCADE)
    val description = varchar("description", 255)
    val exercise = reference("exercise", Exercises, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Equipment(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Equipment>(Equipments)

    var name by Equipments.name
    var resource by Resource referencedOn Equipments.resource
    var description by Equipments.description
    var exercise by Exercise referencedOn Equipments.exercise
    var status by Equipments.status
}