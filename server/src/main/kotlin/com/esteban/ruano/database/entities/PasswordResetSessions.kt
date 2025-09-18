package com.esteban.ruano.database.entities

import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.TimeZone
import java.util.UUID

object PasswordResetSessions : UUIDTable("password_reset_sessions") {
    val userId = reference("user_id", Users)
    val tokenHash = varchar("token_hash", 128).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()
    val createdAt = datetime("created_at")
}

class PasswordResetSession(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PasswordResetSession>(PasswordResetSessions)

    var user by User referencedOn PasswordResetSessions.userId
    var tokenHash by PasswordResetSessions.tokenHash
    var expiresAt by PasswordResetSessions.expiresAt
    var usedAt by PasswordResetSessions.usedAt
    var createdAt by PasswordResetSessions.createdAt

    val isExpired: Boolean get() = getCurrentDateTime(kotlinx.datetime.TimeZone.UTC) > expiresAt
    val isUsed: Boolean get() = usedAt != null

    fun markUsed(now: LocalDateTime = getCurrentDateTime(kotlinx.datetime.TimeZone.UTC)) { usedAt = now }
}