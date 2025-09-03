package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.date
import java.math.BigDecimal
import java.util.*

object SavingsGoals : UUIDTable() {
    val name = varchar("name", 50)
    val targetAmount = decimal("target_amount", 10, 2)
    val currentAmount = decimal("current_amount", 10, 2).default(BigDecimal(0.0))
    val targetDate = date("target_date")
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class SavingsGoal(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SavingsGoal>(SavingsGoals)
    
    var name by SavingsGoals.name
    var targetAmount by SavingsGoals.targetAmount
    var currentAmount by SavingsGoals.currentAmount
    var targetDate by SavingsGoals.targetDate
    var user by User referencedOn SavingsGoals.user
    var status by SavingsGoals.status
} 