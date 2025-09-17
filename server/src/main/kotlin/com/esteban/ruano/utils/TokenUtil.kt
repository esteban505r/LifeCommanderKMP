package com.esteban.ruano.utils

import java.security.SecureRandom
import java.security.MessageDigest
import java.util.Base64

object TokenUtil {
    private val rng = SecureRandom()

    fun newOpaqueToken(bytes: Int = 32): String {
        val b = ByteArray(bytes)
        rng.nextBytes(b)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b) // safe in URLs
    }

    fun sha256Base64Url(input: String, pepper: String? = null): String {
        val md = MessageDigest.getInstance("SHA-256")
        val data = if (pepper != null) (input + pepper).toByteArray() else input.toByteArray()
        val dig = md.digest(data)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(dig)
    }
}
