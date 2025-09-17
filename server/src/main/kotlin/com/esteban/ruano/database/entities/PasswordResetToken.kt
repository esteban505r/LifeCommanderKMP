package com.esteban.ruano.database.entities

import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.UUID

object PasswordResetTokens : UUIDTable("password_reset_tokens") {
    val userId = reference("user_id", Users)
    val tokenHash = varchar("token_hash", 128).index()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")
    val usedAt = datetime("used_at").nullable()
    val createdIp = varchar("created_ip", 45).nullable()
    init {
        index(true, tokenHash)
    }
}

class PasswordResetToken(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PasswordResetToken>(PasswordResetTokens)

    var user by User referencedOn PasswordResetTokens.userId
    var tokenHash by PasswordResetTokens.tokenHash
    var expiresAt by PasswordResetTokens.expiresAt
    var usedAt by PasswordResetTokens.usedAt
    var createdIp by PasswordResetTokens.createdIp
    var createdAt by PasswordResetTokens.createdAt

    val isUsed: Boolean get() = usedAt != null
    val isExpired: Boolean get() = expiresAt < getCurrentDateTime(TimeZone.currentSystemDefault())

    fun markUsed() {
        usedAt =  getCurrentDateTime(TimeZone.currentSystemDefault())
    }
}