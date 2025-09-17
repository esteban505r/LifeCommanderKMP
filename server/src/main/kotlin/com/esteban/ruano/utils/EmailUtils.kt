package com.esteban.ruano.utils/* ---------- Basic validators (tune as needed) ---------- */

fun isLikelyEmail(email: String): Boolean =
    // lightweight sanity check; rely on domain auth + delivery later
    email.contains('@') && email.length in 6..254

 fun isStrongPassword(pwd: String): Boolean {
    // Example: 10+ chars. Adjust with a real policy if needed
    return pwd.length >= 10
}
