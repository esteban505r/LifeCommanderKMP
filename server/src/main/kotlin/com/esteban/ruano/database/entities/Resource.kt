package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.ResourceType
import com.esteban.ruano.database.models.Status

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

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