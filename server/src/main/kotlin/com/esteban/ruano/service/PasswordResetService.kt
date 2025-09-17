package com.esteban.ruano.service

import com.esteban.ruano.database.entities.PasswordResetTokens
import com.esteban.ruano.database.entities.RefreshSessions
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.EmailSender
import com.esteban.ruano.utils.SecurityUtils.hashPassword
import com.esteban.ruano.utils.TokenUtil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.less
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class PasswordResetService(
    private val emailSender: EmailSender,
    private val appBaseUrl: String,
    private val tokenPepper: String? = null
) {
    @OptIn(ExperimentalTime::class)
    fun requestReset(email: String, requesterIp: String?) {
        val normalizedEmail = email.trim().lowercase()
        val userRow = transaction {
            Users
                .select(Users.id)                             // only need the PK
                .where { Users.email eq normalizedEmail }
                .limit(1)
                .firstOrNull()
        } ?: return



        val rawToken = TokenUtil.newOpaqueToken(40)
        val tokenHash = TokenUtil.sha256Base64Url(rawToken, tokenPepper)

        val nowUtc = getCurrentDateTime(TimeZone.UTC)
        val expiresAt = nowUtc.toInstant(TimeZone.UTC).plus(45.minutes).toLocalDateTime(TimeZone.UTC)

        transaction {
            PasswordResetTokens.insert {
                it[userId] = userRow[Users.id]               // EntityID<Int>
                it[PasswordResetTokens.tokenHash] = tokenHash
                it[PasswordResetTokens.expiresAt] = expiresAt
                it[createdIp] = requesterIp
                it[createdAt] = nowUtc
            }
        }

        val link = "$appBaseUrl/reset?token=$rawToken"
        runCatching {
            emailSender.send(
                to = normalizedEmail,
                subject = "Reset your password",
                htmlBody = """
                    <p>We received a request to reset your password.</p>
                    <p><a href="$link">Click here to reset your password</a></p>
                    <p>This link expires in 45 minutes. If you didn’t request this, you can ignore it.</p>
                """.trimIndent(),
                textBody = ""
            )
        }.fold(
            {

            },{
                print(
                    it
                )
            }
        )
    }

    fun resetPassword(rawToken: String, newPassword: String): Boolean {
        val tokenHash = TokenUtil.sha256Base64Url(rawToken.trim(), tokenPepper)
        val nowUtc = getCurrentDateTime(TimeZone.UTC)

        // Select only what we need; no slice()
        val rec = transaction {
            PasswordResetTokens
                .select(PasswordResetTokens.id, PasswordResetTokens.userId, PasswordResetTokens.expiresAt)
                .where {
                    (PasswordResetTokens.tokenHash eq tokenHash) .and(
                            PasswordResetTokens.usedAt.isNull())
                }
                .limit(1)
                .firstOrNull()
        } ?: return false

        if (rec[PasswordResetTokens.expiresAt] < (nowUtc)) return false

        val userEntityId: EntityID<Int> = rec[PasswordResetTokens.userId]
        val newHash = hashPassword(newPassword)

        transaction {
            // Update password
            Users.update({ Users.id eq userEntityId }) {
                it[password] = newHash
                it[updatedAt] = nowUtc
            }
            // Invalidate token
            PasswordResetTokens.update({ PasswordResetTokens.id eq rec[PasswordResetTokens.id] }) {
                it[usedAt] = nowUtc
            }
            // Revoke all active refresh sessions
            RefreshSessions.update({
                (RefreshSessions.userId eq userEntityId) and RefreshSessions.revokedAt.isNull()
            }) {
                it[revokedAt] = nowUtc
            }
        }
        return true
    }

    fun cleanupExpired(): Int = transaction {
        val nowUtc = getCurrentDateTime(TimeZone.currentSystemDefault())
        PasswordResetTokens.deleteWhere {
            (PasswordResetTokens.expiresAt less nowUtc) or PasswordResetTokens.usedAt.isNotNull()
        }
    }

    fun isTokenValid(rawToken: String): Boolean {
        val tokenHash = TokenUtil.sha256Base64Url(rawToken.trim(), tokenPepper)
        val nowUtc = getCurrentDateTime(TimeZone.UTC)

        return transaction {
            PasswordResetTokens
                .select(PasswordResetTokens.expiresAt, PasswordResetTokens.usedAt)
                .where { (PasswordResetTokens.tokenHash eq tokenHash) and PasswordResetTokens.usedAt.isNull() }
                .limit(1)
                .firstOrNull()
                ?.let { row -> row[PasswordResetTokens.expiresAt] > nowUtc } == true
        }
    }
}