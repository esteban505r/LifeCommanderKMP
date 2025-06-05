package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object Portfolios : UUIDTable("portfolios") {
    val title = varchar("title", 255)
    val description = text("description")
    val imageUrl = varchar("image_url", 500).nullable()
    val projectUrl = varchar("project_url", 500).nullable()
    val githubUrl = varchar("github_url", 500).nullable()
    val technologies = array<String>("technologies")
    val category = varchar("category", 100)
    val featured = bool("featured").default(false)
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val createdDate = datetime("created_date").defaultExpression(CurrentDateTime)
    val updatedDate = datetime("updated_date").defaultExpression(CurrentDateTime)
    val userId = reference("user_id", Users, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 10, Status::class).default(Status.ACTIVE)
}

class Portfolio(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Portfolio>(Portfolios)
    
    var title by Portfolios.title
    var description by Portfolios.description
    var imageUrl by Portfolios.imageUrl
    var projectUrl by Portfolios.projectUrl
    var githubUrl by Portfolios.githubUrl
    var technologies by Portfolios.technologies
    var category by Portfolios.category
    var featured by Portfolios.featured
    var startDate by Portfolios.startDate
    var endDate by Portfolios.endDate
    var createdDate by Portfolios.createdDate
    var updatedDate by Portfolios.updatedDate
    var user by User referencedOn Portfolios.userId
    var status by Portfolios.status
} 