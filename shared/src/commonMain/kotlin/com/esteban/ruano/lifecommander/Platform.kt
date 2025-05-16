package com.esteban.ruano

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform