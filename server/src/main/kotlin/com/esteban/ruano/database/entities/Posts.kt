package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

import java.util.*

object Posts : UUIDTable() {
    val title = varchar("title", 255)
    val slug = varchar("slug", 255).uniqueIndex()
    val imageUrl = varchar("image_url", 1024).nullable()
    val description = text("description").nullable()
    val tags = array<String>("tags").default(listOf())
    val categoryId = reference("category_id", PostCategories, ReferenceOption.CASCADE)
    val password = varchar("password", 255).nullable()
    val publishedDate = datetime("published_date").nullable()
    val s3Key = varchar("s3_key", 1024)
}

class Post(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Post>(Posts)

    var title by Posts.title
    var slug by Posts.slug
    var imageUrl by Posts.imageUrl
    var description by Posts.description
    var category by PostCategory referencedOn Posts.categoryId
    var tags by Posts.tags
    var password by Posts.password
    var publishedDate by Posts.publishedDate
    var s3Key by Posts.s3Key
}