package com.esteban.lopez.core_data.helpers

import retrofit2.HttpException
import retrofit2.Response

fun <T> Response<T>.asResult(): Result<T> {
    return if (isSuccessful) {
        val body = body()
        if (body != null) {
            Result.success(body)
        } else {
            // Special case: no body, but success
            @Suppress("UNCHECKED_CAST")
            Result.success(Unit as T)
        }
    } else {
        Result.failure(HttpException(this))
    }
}
