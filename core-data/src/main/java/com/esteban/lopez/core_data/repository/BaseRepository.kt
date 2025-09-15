package com.esteban.ruano.core_data.repository

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.core_data.models.ErrorHandlingUtils

abstract class BaseRepository {

    private val cacheDuration = 60 * 60 * 1000 // 1 hour

    protected suspend fun <T> doRequest(
        lastFetchTime: Long,
        isNetworkAvailable: Boolean,
        offlineModeEnabled: Boolean = false,
        forceRefresh: Boolean = false,
        localFetch: suspend () -> T,
        remoteFetch: suspend () -> T,
        onSuccess: suspend (T) -> Unit = {}
    ): Result<T> {
        val offlineModeReplacement = false
        return ErrorHandlingUtils.handleDataError {
            if (!offlineModeReplacement && shouldFetchFromNetwork(lastFetchTime,
                    isNetworkAvailable, forceRefresh)) {
                val data = remoteFetch()
                onSuccess(data)
                Result.success(data)
            } else {
                val data = localFetch()
                onSuccess(data)
                Result.success(data)
            }
        }
    }

    protected suspend fun <T> doLocalRequest(
        localFetch: suspend () -> T
    ): Result<T> {
        return ErrorHandlingUtils.handleDataError {
            val data = localFetch()
            Result.success(data)
        }
    }

    suspend fun <T> doRemoteRequest(
        isNetworkAvailable: Boolean,
        remoteFetch: suspend () -> T
    ): Result<T> {
        return ErrorHandlingUtils.handleDataError {
            if (isNetworkAvailable) {
                val data = remoteFetch()
                Result.success(data)
            } else {
                Result.failure(DataException.Network.NoInternet)
            }
        }
    }

    private fun shouldFetchFromNetwork(
        lastFetchTime: Long,
        isNetworkAvailable: Boolean,
        forceRefresh: Boolean
    ): Boolean {
        val isDataOld = (System.currentTimeMillis() - lastFetchTime) > cacheDuration
        return isNetworkAvailable && (forceRefresh || isDataOld)
    }
}
