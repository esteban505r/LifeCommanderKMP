package com.esteban.ruano.workout_presentation.utils

import android.content.Context
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.workout_domain.model.MuscleGroup

fun MuscleGroup.toResourceString(context:Context): String {
    return when (this) {
        MuscleGroup.UPPER_BODY -> context.getString(R.string.upper_body)
        MuscleGroup.LOWER_BODY -> context.getString(R.string.lower_body)
        MuscleGroup.FULL_BODY -> context.getString(R.string.full_body)
        MuscleGroup.CORE -> context.getString(R.string.core)
        MuscleGroup.CARDIO -> context.getString(R.string.cardio)
        MuscleGroup.STRETCH -> context.getString(R.string.stretch)
        MuscleGroup.LEGS -> context.getString(R.string.legs)
        MuscleGroup.PULL -> context.getString(R.string.pull)
        MuscleGroup.PUSH -> context.getString(R.string.push)
        MuscleGroup.NONE -> context.getString(R.string.none)
        MuscleGroup.BACK -> context.getString(R.string.back)
    }
}