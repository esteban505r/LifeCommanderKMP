package com.esteban.ruano.service

import com.esteban.ruano.database.entities.PasswordResetPins
import com.esteban.ruano.database.entities.PasswordResetSessions
import com.esteban.ruano.database.entities.RefreshSessions
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.EmailSender
import com.esteban.ruano.utils.SecurityUtils.hashPassword
import io.ktor.server.plugins.BadRequestException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class PasswordResetService(
    private val emailSender: EmailSender,
    private val appBaseUrl: String,
    private val tokenPepper: String? = null
) {

    /* Step 1: request PIN (email) */
    @OptIn(ExperimentalTime::class)
    fun requestReset(email: String, requesterIp: String?) {
        val normalizedEmail = email.trim().lowercase()
        val userRow = transaction {
            Users
                .select(Users.id)
                .where { Users.email eq normalizedEmail }
                .limit(1).firstOrNull()
        } ?: throw BadRequestException("This email doesn't belong to any user")

        val nowUtc = getCurrentDateTime(TimeZone.UTC)
        val expiresAt =
            nowUtc.toInstant(TimeZone.UTC).plus(10.minutes).toLocalDateTime(TimeZone.UTC) // PIN valid for 10 minutes

        val rawPin = newSixDigitPin()
        val pinHash = sha256B64(rawPin, tokenPepper)

        transaction {
            PasswordResetPins.insert {
                it[userId] = userRow[Users.id]
                it[PasswordResetPins.pinHash] = pinHash
                it[attempts] = 0
                it[maxAttempts] = 5
                it[this.expiresAt] = expiresAt
                it[verifiedAt] = null
                it[usedAt] = null
                it[createdAt] = nowUtc
                it[createdIp] = requesterIp
            }
        }

        // Email the PIN (not the hash)
        val html = templateResetEmailHtml(
            pin = rawPin,
            minutesValid = 10,
            requestIp = requesterIp
            // appName/siteUrl/supportEmail/otterImageUrl can be customized here
        )
        val text = templateResetEmailText(
            pin = rawPin,
            minutesValid = 10,
            requestIp = requesterIp
        )

        emailSender.send(
            to = normalizedEmail,
            subject = "Oter | Your password reset code",
            htmlBody = html,
            textBody = text
        )

    }

    /* Step 2: verify PIN -> return a short-lived reset session token (raw) */
    @OptIn(ExperimentalTime::class)
    fun verifyPin(email: String, pin: String): String? {
        val normalizedEmail = email.trim().lowercase()
        val nowUtc = getCurrentDateTime(TimeZone.UTC)
        val userId = transaction {
            Users.select(Users.id).where { Users.email eq normalizedEmail }.limit(1)
                .firstOrNull()?.get(Users.id)
        } ?: return null

        val rec = transaction {
            PasswordResetPins
                .select(
                    PasswordResetPins.id,
                    PasswordResetPins.pinHash,
                    PasswordResetPins.attempts,
                    PasswordResetPins.maxAttempts,
                    PasswordResetPins.expiresAt,
                    PasswordResetPins.verifiedAt
                )
                .where {
                    (PasswordResetPins.userId eq userId) and
                            PasswordResetPins.verifiedAt.isNull() and
                            (PasswordResetPins.expiresAt greater  nowUtc)
                }
                .orderBy(PasswordResetPins.createdAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()
        } ?: return null

        // attempts / expiry
        if (rec[PasswordResetPins.attempts] >= rec[PasswordResetPins.maxAttempts]) return null

        val ok = slowEquals(
            sha256B64(pin.trim(), tokenPepper),
            rec[PasswordResetPins.pinHash]
        )

        transaction {
            // increment attempts regardless (throttling; but only on failure)
            if (!ok) {
                PasswordResetPins.update({ PasswordResetPins.id eq rec[PasswordResetPins.id] }) {
                    it[attempts] = rec[PasswordResetPins.attempts] + 1
                }
            }
        }
        if (!ok) return null

        // Mark verified
        transaction {
            PasswordResetPins.update({ PasswordResetPins.id eq rec[PasswordResetPins.id] }) {
                it[verifiedAt] = nowUtc
            }
        }

        // Create a reset session token (opaque) valid for, say, 15 min
        val rawSession = newOpaqueToken(40)
        val sessionHash = sha256B64(rawSession, tokenPepper)
        val sessionExpiry = nowUtc.toInstant(TimeZone.UTC).plus(15.minutes).toLocalDateTime(TimeZone.UTC)
        transaction {
            PasswordResetSessions.insert {
                it[this.userId] = userId
                it[tokenHash] = sessionHash
                it[expiresAt] = sessionExpiry
                it[createdAt] = nowUtc
            }
        }
        return rawSession // client will use this in /reset-password
    }

    /* Step 3: consume reset session, set new password */
    fun resetPassword(resetSessionToken: String, newPassword: String): Boolean {
        val nowUtc = getCurrentDateTime(TimeZone.UTC)
        val tokenHash = sha256B64(resetSessionToken.trim(), tokenPepper)

        val session = transaction {
            PasswordResetSessions
                .select(
                    PasswordResetSessions.id,
                    PasswordResetSessions.userId,
                    PasswordResetSessions.expiresAt,
                    PasswordResetSessions.usedAt
                )
                .where {
                    (PasswordResetSessions.tokenHash eq tokenHash) and
                            PasswordResetSessions.usedAt.isNull() and
                            (PasswordResetSessions.expiresAt greater nowUtc)
                }
                .limit(1)
                .firstOrNull()
        } ?: return false

        val userId = session[PasswordResetSessions.userId]
        val newHash = hashPassword(newPassword)

        transaction {
            Users.update({ Users.id eq userId }) {
                it[password] = newHash
                it[updatedAt] = nowUtc
            }
            PasswordResetSessions.update({ PasswordResetSessions.id eq session[PasswordResetSessions.id] }) {
                it[usedAt] = nowUtc
            }
            RefreshSessions.update({
                (RefreshSessions.userId eq userId) and RefreshSessions.revokedAt.isNull()
            }) {
                it[revokedAt] = nowUtc
            }
        }
        return true
    }

    // --- helpers ---
    private fun newSixDigitPin(): String {
        // 6 digits, allow leading zeros
        val n = Random.nextInt(0, 1_000_000)
        return "%06d".format(n)
    }

    private fun newOpaqueToken(bytes: Int = 32): String {
        val b = ByteArray(bytes)
        java.security.SecureRandom().nextBytes(b)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b)
    }

    private fun sha256B64(value: String, pepper: String?): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val dig = md.digest((value + (pepper ?: "")).toByteArray())
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(dig)
    }

    private fun slowEquals(a: String, b: String): Boolean {
        // constant-time compare
        var r = 0
        val x = a.toByteArray()
        val y = b.toByteArray()
        val len = maxOf(x.size, y.size)
        for (i in 0 until len) {
            val xb = if (i < x.size) x[i] else 0
            val yb = if (i < y.size) y[i] else 0
            r = r or (xb.toInt() xor yb.toInt())
        }
        return r == 0
    }

    private fun templateResetEmailHtml(
        pin: String,
        appName: String = "Esteban Ruano",
        siteUrl: String = "https://estebanruano.com",
        minutesValid: Int = 10,
        supportEmail: String = "contact@estebanruano.com",
        requestIp: String? = null,
        otterImageUrl: String = "https://res.cloudinary.com/dymzwuonk/image/upload/v1758142521/s7vltbuglwi4nyex4g1p.png"
    ): String {
        val digits = pin.trim().map { it.toString() }
        return """
<!DOCTYPE html>
<html lang="en" style="margin:0;padding:0;">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>$appName • Password Reset Code</title>
    <style>
      body{margin:0;padding:0;background:#f6f7fb;color:#111827;}
      a{color:#2563eb;text-decoration:none;}
      img{border:0;outline:none;text-decoration:none;display:block;max-width:100%;height:auto;}
      .wrapper{width:100%;padding:32px 12px;}
      .card{
        width:100%;max-width:760px;margin:0 auto;background:#ffffff;border:1px solid #eef1f6;
        border-radius:24px;box-shadow:0 8px 28px rgba(16,24,40,.08);overflow:hidden;
        font:14px/1.6 ui-sans-serif,system-ui,-apple-system,Segoe UI,Roboto,Arial;
      }
      .brandbar{padding:14px 18px;background:linear-gradient(90deg,#f8fafc, #eef2ff); border-bottom:1px solid #eef1f6;}
      .pill{display:inline-flex;align-items:center;gap:10px;padding:6px 12px;border-radius:999px;background:#fff;border:1px solid #e5e7eb;box-shadow:0 1px 6px rgba(16,24,40,.06);color:#0f172a;font-weight:600;}
      .avatar{width:22px;height:22px;border-radius:999px;display:inline-flex;align-items:center;justify-content:center;color:#fff;background:#2563eb;font:700 12px/1 ui-sans-serif;}
      h1{margin:0 0 10px;font:700 24px/1.25 ui-sans-serif;color:#0f172a;letter-spacing:-.2px;}
      p{margin:0 0 14px;color:#374151;}
      /* Code: table so it NEVER wraps */
      .code-table{margin:22px auto 12px;border-collapse:separate;border-spacing:8px;}
      .code-td{
        width:52px;height:56px;border:1px solid #e5e7eb;border-radius:12px;background:#f9fafb;text-align:center;
        font:800 22px/56px ui-sans-serif;color:#111827;
      }
      .meta{margin-top:6px;font-size:12px;color:#6b7280;text-align:center;}
      .hr{height:1px;background:#eef1f6;margin:18px 0 0;}
      .footer{padding:14px 28px 26px;color:#6b7280;font-size:12px;}
      .muted{color:#9ca3af;}
      @media (prefers-color-scheme: dark){
        body{background:#0b1020;color:#e5e7eb;}
        .card{background:#0f172a;border-color:#1f2937;box-shadow:none;}
        .brandbar{background:linear-gradient(90deg,#0f172a,#0b1020);border-bottom-color:#1f2937;}
        .pill{background:#111827;border-color:#1f2937;color:#e5e7eb;}
        .avatar{background:#3b82f6;}
        h1{color:#e5e7eb;} p{color:#cbd5e1;}
        .code-td{background:#0b1020;border-color:#1f2937;color:#e5e7eb;}
        .hr{background:#1f2937;} .footer{color:#94a3b8;} a{color:#60a5fa;}
      }
      @media only screen and (max-width: 520px){
        .right{padding:22px 18px !important;}
        .left{width:100% !important; display:block !important;}
        .right{width:100% !important; display:block !important;}
      }
    </style>
  </head>
  <body>
    <div style="display:none;visibility:hidden;opacity:0;height:0;overflow:hidden;">
      Your $appName password reset code is $pin. It expires in $minutesValid minutes.
    </div>

    <div class="wrapper">
      <table class="card" role="presentation" cellspacing="0" cellpadding="0" width="100%">
        <tr>
          <td class="brandbar">
            <span class="pill">
              <span class="avatar">${appName.split(" ").map { it.first() }.joinToString("").take(2).uppercase()}</span>
              $appName
            </span>
          </td>
        </tr>

        <tr>
          <td>
            <!-- Two fixed columns: left 300px, right auto -->
            <table role="presentation" cellspacing="0" cellpadding="0" width="100%">
              <tr>
                <td class="left" width="300" valign="top" style="background:#0ea5e9;">
                  <img src="$otterImageUrl" alt="Friendly otter" style="border-top-left-radius:24px;width:100%;height:auto;" />
                </td>

                <td class="right" valign="top" style="padding:26px 28px;">
                  <h1>Here’s your reset code</h1>
                  <p>Use the 6-digit code below to continue resetting your password. For your security, it expires in <b>$minutesValid minutes</b>.</p>

                  <!-- Code: one row, six cells, cannot wrap -->
                  <table class="code-table" role="presentation" cellspacing="0" cellpadding="0">
                    <tr>
                      <td class="code-td">${digits.getOrNull(0) ?: ""}</td>
                      <td class="code-td">${digits.getOrNull(1) ?: ""}</td>
                      <td class="code-td">${digits.getOrNull(2) ?: ""}</td>
                      <td class="code-td">${digits.getOrNull(3) ?: ""}</td>
                      <td class="code-td">${digits.getOrNull(4) ?: ""}</td>
                      <td class="code-td">${digits.getOrNull(5) ?: ""}</td>
                    </tr>
                  </table>

                  <p class="meta">Didn’t request this? You can ignore this email and your password will stay the same.</p>
                  <p style="margin-top:14px;">Or return to <a href="$siteUrl">$siteUrl</a></p>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <tr><td class="hr"></td></tr>

        <tr>
          <td class="footer">
            ${requestIp?.let { """Request from IP: <span class="muted">$it</span><br/>""" } ?: ""}
            Need help? Reply to <a href="mailto:$supportEmail">$supportEmail</a>.<br/>
            <span class="muted">© ${java.time.Year.now()} $appName. All rights reserved.</span>
          </td>
        </tr>
      </table>
    </div>
  </body>
</html>
""".trimIndent()
        }

    private fun templateResetEmailText(
        pin: String,
        minutesValid: Int = 10,
        siteUrl: String = "https://estebanruano.com",
        requestIp: String? = null,
        supportEmail: String = "contact@estebanruano.com"
    ) = buildString {
        appendLine("Your password reset code: $pin")
        appendLine("This code expires in $minutesValid minutes.")
        if (requestIp != null) appendLine("Request IP: $requestIp")
        appendLine()
        appendLine("If you didn’t request this, you can ignore this message.")
        appendLine("Return to: $siteUrl")
        appendLine("Help: $supportEmail")
    }
}