package com.esteban.ruano.onboarding_data.repository

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.onboarding_data.datasources.AuthDataSource
import com.esteban.ruano.onboarding_domain.model.LoginModel
import com.esteban.ruano.onboarding_domain.repository.AuthRepository

class AuthRepositoryImpl(
    val remoteDataSource: AuthDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
): BaseRepository(), AuthRepository {
    override suspend fun login(email: String, password: String, fcmToken: String?, timezone: String?): Result<LoginModel> {
        return doRemoteRequest(
            remoteFetch = {
                remoteDataSource.login(email, password, fcmToken, timezone)
            },
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
        )
    }

    override suspend fun register(email: String, password: String, name: String) : Result<Unit> {
      return  doRemoteRequest(
            remoteFetch = {
                remoteDataSource.register(email, password, name)
            },
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
        )
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return doRemoteRequest(
            remoteFetch = {
                remoteDataSource.forgotPassword(email) // POST /forgot-password (no body in Result)
            },
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
        )
    }

    override suspend fun verifyResetPin(email: String, pin: String): Result<String> {
        return doRemoteRequest(
            remoteFetch = {
                remoteDataSource.verifyResetPin(email, pin) // POST /reset-password/verify -> returns reset_token:String
            },
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
        )
    }

    override suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Result<Unit> {
        return doRemoteRequest(
            remoteFetch = {
                remoteDataSource.resetPassword(resetToken, newPassword) // POST /reset-password
            },
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
        )
    }

}