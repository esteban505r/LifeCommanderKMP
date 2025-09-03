package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.*

object Portfolios : UUIDTable("portfolios") {
    val title = varchar("title", 255)
    val description = text("description").default("")
    val extended_description = text("extended_description").default("")
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
    var extendedDescription by Portfolios.extended_description
    var startDate by Portfolios.startDate
    var endDate by Portfolios.endDate
    var createdDate by Portfolios.createdDate
    var updatedDate by Portfolios.updatedDate
    var user by User referencedOn Portfolios.userId
    var status by Portfolios.status
} 