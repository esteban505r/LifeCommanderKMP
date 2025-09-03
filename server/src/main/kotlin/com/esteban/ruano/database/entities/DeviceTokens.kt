package com.esteban.ruano.database.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object DeviceTokens : UUIDTable() {
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val token = varchar("token", 255)
    val platform = varchar("platform", 10)
    val updatedAt = datetime("updated_at")
}

class DeviceToken(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DeviceToken>(DeviceTokens)

    var user by User referencedOn DeviceTokens.user
    var token by DeviceTokens.token
    var platform by DeviceTokens.platform
    var updatedAt by DeviceTokens.updatedAt
} 