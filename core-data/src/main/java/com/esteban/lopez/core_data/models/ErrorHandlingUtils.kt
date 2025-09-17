package com.esteban.ruano.core_data.models

import android.util.Log
import com.esteban.ruano.core.domain.model.DataException
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

object ErrorHandlingUtils {

    suspend fun <T> handleDataError(action: suspend () -> Result<T>): Result<T> {
        return try {
            action()
        } catch (e: Exception) {
            Log.e("Error: DataError", e.stackTraceToString()?:"")
            when (e) {
                is HttpException -> {
                    when (e.code()) {
                        404, 500 -> Result.failure(DataException.Network.ServerException)
                        else -> Result.failure(DataException.Network.Unknown)
                    }
                }
                is IOException -> Result.failure(DataException.Network.NoInternet)
                is NullPointerException -> Result.failure(DataException.Local.NotFound())
                else -> Result.failure(DataException.Network.Unknown)
            }
        }
    }

}