package com.esteban.ruano.workout_data.mappers

import com.esteban.ruano.workout_data.remote.dto.ResourceResponse
import com.esteban.ruano.workout_domain.model.Resource

fun ResourceResponse.toResource(): Resource {
    return Resource(
        id = id,
        url = url,
        type = type
    )
}

fun Resource.toResourceResponse(): ResourceResponse {
    return ResourceResponse(
        id = id,
        url = url,
        type = type
    )
}