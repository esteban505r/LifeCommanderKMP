package com.esteban.ruano.workout_data.mappers

import com.esteban.ruano.workout_data.remote.dto.EquipmentResponse
import com.esteban.ruano.workout_domain.model.Equipment

fun EquipmentResponse.toEquipment(): Equipment {
    return Equipment(
        id = id,
        name = name,
        description = description,
        resource = resourceResponse.toResource()
    )
}

fun Equipment.toEquipmentResponse(): EquipmentResponse {
    return EquipmentResponse(
        id = id,
        name = name,
        description = description,
        resourceResponse = resource.toResourceResponse()
    )
}