package com.esteban.ruano.onboarding_data.remote

import com.esteban.ruano.onboarding_data.remote.dto.LoginRequest
import com.esteban.ruano.onboarding_data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body loginRequest:LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Query("email") email: String, @Query("password") password: String, @Query("name") name: String): Unit

}