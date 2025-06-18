package com.esteban.ruano.database.entities

import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*


object CategoryKeywords : UUIDTable("category_keywords") {
    val category = enumerationByName<Category>(
        "category",
        50,
        Category::class
    )
    val keyword = varchar("keyword", 255)
    val user = reference("user", Users)
    val status = enumerationByName("status", 50, Status::class).default(Status.ACTIVE)
    val createdAt = datetime("created_at").default(getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ))
    val updatedAt = datetime("updated_at").default(getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ))
}

class CategoryKeyword(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CategoryKeyword>(CategoryKeywords)

    var category by CategoryKeywords.category
    var keyword by CategoryKeywords.keyword
    var user by User referencedOn CategoryKeywords.user
    var status by CategoryKeywords.status
    var createdAt by CategoryKeywords.createdAt
    var updatedAt by CategoryKeywords.updatedAt
}

