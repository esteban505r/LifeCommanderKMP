package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Tag
import com.esteban.ruano.models.tags.TagDTO

fun Tag.toDTO(): TagDTO {
    return TagDTO(
        id = this.id.toString(),
        name = this.name,
        slug = this.slug,
        color = this.color
    )
}

