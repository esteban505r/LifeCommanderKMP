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

object PasswordResetPins : UUIDTable("password_reset_pins") {
    val userId = reference("user_id", Users)
    val pinHash = varchar("pin_hash", 128).index()       // hash of the 6-digit PIN
    val attempts = integer("attempts").default(0)
    val maxAttempts = integer("max_attempts").default(5)
    val expiresAt = datetime("expires_at")
    val verifiedAt = datetime("verified_at").nullable()  // when PIN was verified
    val usedAt = datetime("used_at").nullable()          // optional; mark consumed
    val createdAt = datetime("created_at")
    val createdIp = varchar("created_ip", 45).nullable()
    init {
        // prevent identical active codes (optional)
        index(false, userId, expiresAt)
    }
}

class PasswordResetPin(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PasswordResetPin>(PasswordResetPins)

    var user by User referencedOn PasswordResetPins.userId
    var pinHash by PasswordResetPins.pinHash
    var attempts by PasswordResetPins.attempts
    var maxAttempts by PasswordResetPins.maxAttempts
    var expiresAt by PasswordResetPins.expiresAt
    var verifiedAt by PasswordResetPins.verifiedAt
    var usedAt by PasswordResetPins.usedAt
    var createdAt by PasswordResetPins.createdAt
    var createdIp by PasswordResetPins.createdIp

    val isExpired: Boolean get() = getCurrentDateTime(kotlinx.datetime.TimeZone.UTC) > expiresAt
    val isVerified: Boolean get() = verifiedAt != null
    val isUsed: Boolean get() = usedAt != null
    val remainingAttempts: Int get() = (maxAttempts - attempts).coerceAtLeast(0)

    fun markVerified(now: LocalDateTime =  getCurrentDateTime(kotlinx.datetime.TimeZone.UTC)) { verifiedAt = now }
    fun incrementAttempts() { attempts += 1 }
    fun markUsed(now: LocalDateTime =  getCurrentDateTime(kotlinx.datetime.TimeZone.UTC))  { usedAt = now }
}