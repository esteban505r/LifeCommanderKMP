package com.esteban.ruano.utils

import at.favre.lib.crypto.bcrypt.BCrypt
import com.esteban.ruano.models.users.RegisterUserDTO

object SecurityUtils {
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun checkPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }

    fun RegisterUserDTO.hashPassword(): RegisterUserDTO {
        return copy(password = hashPassword(password))
    }

}