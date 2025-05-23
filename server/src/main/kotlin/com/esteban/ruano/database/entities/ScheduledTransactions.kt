package com.esteban.ruano.database.entities

import com.esteban.ruano.database.entities.Accounts.reference
import com.esteban.ruano.database.models.Status
import com.lifecommander.finance.model.TransactionType
import com.lifecommander.models.Frequency
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object ScheduledTransactions : UUIDTable() {
    val description = varchar("description", 255)
    val amount = decimal("amount", 10, 2)
    val startDate = datetime("start_date")
    val frequency = enumerationByName("frequency", 20, Frequency::class)
    val interval = integer("interval").default(1)
    val type = enumerationByName("type", 20, TransactionType::class)
    val category = varchar("category", 50)
    val account = reference("account_id", Accounts)
    val applyAutomatically = bool("apply_automatically").default(false)
    val user = reference("user_id", Users)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class ScheduledTransaction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScheduledTransaction>(ScheduledTransactions)

    var user by User referencedOn ScheduledTransactions.user
    var description by ScheduledTransactions.description
    var amount by ScheduledTransactions.amount
    var startDate by ScheduledTransactions.startDate
    var frequency by ScheduledTransactions.frequency
    var interval by ScheduledTransactions.interval
    var type by ScheduledTransactions.type
    var category by ScheduledTransactions.category
    var account by Account referencedOn ScheduledTransactions.account
    var applyAutomatically by ScheduledTransactions.applyAutomatically
    var status by ScheduledTransactions.status
}
