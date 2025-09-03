package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.ResourceType
import com.esteban.ruano.database.models.Status

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object Resources : IntIdTable() {

    val url = varchar("url", 255)
    val type = enumerationByName("resource_type", 50, ResourceType::class)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Resource(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Resource>(Resources)

    var url by Resources.url
    var type by Resources.type
    var status by Resources.status
}