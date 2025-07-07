package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val name = varchar("name", 50)
    val email = varchar("email", 255)
    val password = varchar("password", 255)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val timeZone = varchar("time_zone", 50).default("UTC")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var email by Users.email
    var password by Users.password
    var status by Users.status
    var timeZone by Users.timeZone
}