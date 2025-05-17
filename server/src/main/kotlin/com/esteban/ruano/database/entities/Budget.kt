package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import com.lifecommander.models.Frequency
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date
import java.util.UUID

object Budgets : UUIDTable() {
    val name = varchar("name", 50)
    val amount = decimal("amount", 10, 2)
    val category = varchar("category", 50)
    val startDate = date("start_date")
    val frequency = enumerationByName( "frequency", 15, Frequency::class).default(Frequency.MONTHLY)
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Budget(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Budget>(Budgets)
    
    var name by Budgets.name
    var amount by Budgets.amount
    var category by Budgets.category
    var startDate by Budgets.startDate
    var user by User referencedOn Budgets.user
    var status by Budgets.status
} 