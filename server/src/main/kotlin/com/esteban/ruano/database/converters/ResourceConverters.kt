package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Resource
import com.esteban.ruano.models.resource.ResourceDTO

fun Resource.toDTO():ResourceDTO{
    return ResourceDTO(
        id = this.id.value,
        url = this.url,
        type = this.type.value
    )
}