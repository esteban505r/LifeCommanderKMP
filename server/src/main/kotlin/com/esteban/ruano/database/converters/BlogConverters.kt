package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Post
import com.esteban.ruano.database.entities.Reminder
import com.esteban.ruano.models.blog.PostResponse
import com.esteban.ruano.models.reminders.CreateReminderDTO
import com.esteban.ruano.models.reminders.ReminderDTO
import com.esteban.ruano.models.tasks.PostDTO

fun PostResponse.toDTO(): PostDTO {
    return PostDTO(
        id = this.id,
        title = this.title,
        slug = this.slug,
        publishedDate = this.publishedDate
    )
}

fun Post.toDTO(): PostDTO {
    return PostDTO(
        id = this.id.value.toString(),
        title = this.title,
        slug = this.slug,
        publishedDate = this.publishedDate.toString()
    )
}