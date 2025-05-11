package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.User
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.users.LoginUserDTO
import com.esteban.ruano.models.users.RegisterUserDTO

fun User.toRegisterDTO(): RegisterUserDTO {
    return RegisterUserDTO(
        name = this.name,
        email = this.email,
        password = this.password
    )
}

fun User.toLoggedDTO(): LoggedUserDTO {
    return LoggedUserDTO(
        id = this.id.value,
        email = this.email,
    )
}

fun User.toLoginUserDTO(): LoginUserDTO {
    return LoginUserDTO(
        id = this.id.value,
        email = this.email,
        password = this.password
    )
}