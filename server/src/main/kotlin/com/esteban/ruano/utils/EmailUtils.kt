package com.esteban.ruano.utils/* ---------- Basic validators (tune as needed) ---------- */

private val LOCAL_PART_REGEX = Regex("^[A-Za-z0-9._%+-]{1,64}$")
private val DOMAIN_PART_REGEX = Regex("^(?=.{1,255}$)([A-Za-z0-9-]+\\.)+[A-Za-z]{2,63}$")
fun String.isLikelyEmail(): Boolean {
    if (length !in 6..254) return false
    val atIndex = indexOf('@')
    if (atIndex <= 0 || atIndex == lastIndex) return false

    val localPart = substring(0, atIndex)
    val domainPart = substring(atIndex + 1)

    if (!LOCAL_PART_REGEX.matches(localPart)) return false
    if (!DOMAIN_PART_REGEX.matches(domainPart)) return false

    return true
}

 fun isStrongPassword(pwd: String): Boolean {
    // Example: 10+ chars. Adjust with a real policy if needed
    return pwd.length >= 10
}
