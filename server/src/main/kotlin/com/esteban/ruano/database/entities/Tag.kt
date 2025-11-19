package com.esteban.ruano.database.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

object Tags : UUIDTable() {
    val name = varchar("name", 50)
    val slug = varchar("slug", 60)
    val color = varchar("color", 7).nullable()
    val user = reference("user_id", Users, ReferenceOption.CASCADE)
    
    init {
        uniqueIndex(user, slug) // Ensure slug uniqueness per user
    }
}

class Tag(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Tag>(Tags)
    
    var name by Tags.name
    var slug by Tags.slug
    var color by Tags.color
    var user by User referencedOn Tags.user
    var tasks by Task via TaskTags
}

object TaskTags : UUIDTable("task_tags") {
    val task = reference("task_id", Tasks, ReferenceOption.CASCADE)
    val tag = reference("tag_id", Tags, ReferenceOption.CASCADE)
    
    init {
        uniqueIndex(task, tag) // Composite primary key
    }
}

