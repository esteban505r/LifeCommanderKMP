package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import com.lifecommander.finance.model.TransactionType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object Transactions : UUIDTable() {
    val amount = decimal("amount", 10, 2)
    val description = varchar("description", 255)
    val date = datetime("date")
    val type = enumerationByName("type", 20, TransactionType::class)
    val category = varchar("category", 50)
    val account = reference("account_id", Accounts, ReferenceOption.CASCADE)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Transaction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Transaction>(Transactions)
    
    var amount by Transactions.amount
    var description by Transactions.description
    var date by Transactions.date
    var type by Transactions.type
    var category by Transactions.category
    var account by Account referencedOn Transactions.account
    var user by User referencedOn Transactions.user
    var status by Transactions.status
} 