package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.AccountType
import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object Accounts : UUIDTable() {
    val name = varchar("name", 50)
    val type = enumerationByName("type", 20, AccountType::class)
    val initialBalance = decimal("balance", 10, 2)
    val currency = varchar("currency", 3).default("USD")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Account(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Account>(Accounts)
    
    var name by Accounts.name
    var type by Accounts.type
    var initialBalance by Accounts.initialBalance
    var currency by Accounts.currency
    var user by User referencedOn Accounts.user
    var status by Accounts.status
} 