package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import com.lifecommander.models.Frequency
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.date
import java.util.*

object Budgets : UUIDTable() {
    val name = varchar("name", 50)
    val amount = decimal("amount", 10, 2)
    val category = varchar("category", 50)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
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
    var endDate by Budgets.endDate
    var frequency by Budgets.frequency
    var user by User referencedOn Budgets.user
    var status by Budgets.status
} 