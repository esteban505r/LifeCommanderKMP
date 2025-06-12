package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object PostCategories : UUIDTable("post_categories") {
    val name = varchar("name", 100).uniqueIndex()
    val slug = varchar("slug", 100).uniqueIndex()
    val description = text("description").nullable()
    val color = varchar("color", 7).nullable() // For hex color codes like #FF5733
    val icon = varchar("icon", 50).nullable() // For icon names/classes
    val displayOrder = integer("display_order").default(0)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
    val password = varchar("password", 255).nullable() // Password protection for category
    val createdDate = datetime("created_date").defaultExpression(CurrentDateTime)
    val updatedDate = datetime("updated_date").defaultExpression(CurrentDateTime)
}

class PostCategory(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PostCategory>(PostCategories)

    var name by PostCategories.name
    var slug by PostCategories.slug
    var description by PostCategories.description
    var color by PostCategories.color
    var icon by PostCategories.icon
    var displayOrder by PostCategories.displayOrder
    var status by Habits.status
    var password by PostCategories.password
    var createdDate by PostCategories.createdDate
    var updatedDate by PostCategories.updatedDate
} 