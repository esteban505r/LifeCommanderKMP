package com.esteban.ruano.core.interfaces

fun interface OperationListener{
    suspend fun onOperation(): Boolean
}