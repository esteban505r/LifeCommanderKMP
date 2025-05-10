package com.esteban.ruano.core.domain.model

sealed class Gender(val name:String) {
    data object Male : Gender("male")
    data object Female : Gender("female")

    companion object{
        fun fromString(value:String):Gender{
            return when(value){
                "male"-> Male
                "female"->Female
                else -> Female
            }
        }
    }
}