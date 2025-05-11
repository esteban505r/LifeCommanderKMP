package com.esteban.ruano.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

import java.util.*

object Posts : UUIDTable() {
    val title = varchar("title", 255)
    val slug = varchar("slug", 255).uniqueIndex()
    val publishedDate = datetime("published_date").nullable()
    val s3Key = varchar("s3_key", 1024)
}

class Post(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Post>(Posts)

    var title by Posts.title
    var slug by Posts.slug
    var publishedDate by Posts.publishedDate
    var s3Key by Posts.s3Key
}