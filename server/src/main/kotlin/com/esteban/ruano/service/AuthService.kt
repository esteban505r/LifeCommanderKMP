package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toLoggedDTO
import com.esteban.ruano.database.converters.toLoginUserDTO
import com.esteban.ruano.database.entities.User
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.users.RegisterUserDTO
import com.esteban.ruano.utils.FirstRunSeeder
import com.esteban.ruano.utils.SecurityUtils
import com.esteban.ruano.utils.isLikelyEmail
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class AuthService(
    val seeder: FirstRunSeeder
) : BaseService() {

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
        val userId = transaction {
            val insertedRow = Users.insert {
                it[name] = user.name
                it[email] = user.email
                it[password] = user.password
            }.resultedValues?.firstOrNull()

            insertedRow?.get(Users.id)?.value
        } ?: throw BadRequestException("User not registered")

        seeder.seedForNewUser(userId)
        return true
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