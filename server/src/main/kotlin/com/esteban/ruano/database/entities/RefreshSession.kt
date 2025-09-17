package com.esteban.ruano.database.entities

import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.UUID

object RefreshSessions : UUIDTable("refresh_sessions") {
    val userId = reference("user_id", Users)
    val revokedAt = datetime("revoked_at").nullable()

    val createdAt = datetime("created_at")
}

class RefreshSessionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RefreshSessionEntity>(RefreshSessions)

    var user by User referencedOn RefreshSessions.userId
    var createdAt by RefreshSessions.createdAt
    var revokedAt by RefreshSessions.revokedAt

    val isActive: Boolean get() = revokedAt == null

    fun revoke() {
        revokedAt = getCurrentDateTime(TimeZone.currentSystemDefault())
    }
}