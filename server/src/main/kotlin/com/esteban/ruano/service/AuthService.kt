package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toLoggedDTO
import com.esteban.ruano.database.converters.toLoginUserDTO
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.users.RegisterUserDTO
import com.esteban.ruano.utils.SecurityUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class AuthService : BaseService() {

    fun login(email: String, password: String): LoggedUserDTO? {
        val user = transaction {
            User.find { Users.email eq email}
                .firstOrNull()?.toLoginUserDTO()
        }

        user ?: return null

        val verified = SecurityUtils.checkPassword(password, user.password)
        return if (verified) user.id?.let { LoggedUserDTO(it,user.email) } else null
    }

    fun register(user: RegisterUserDTO): Boolean {
        return transaction {
            val insertedRow = Users.insert {
                it[name] = user.name
                it[email] = user.email
                it[password] = user.password
            }
            insertedRow.resultedValues != null
        }
    }

    fun findByID(id: Int): LoggedUserDTO? {
        return transaction {
            User.findById(id)?.toLoggedDTO()
        }
    }

    fun findByEmail(email: String): LoggedUserDTO? {
        return transaction {
            User.find { Users.email eq email }
                .firstOrNull()?.toLoggedDTO()
        }
    }
}