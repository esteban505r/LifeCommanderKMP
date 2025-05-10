package com.esteban.ruano.core_data.di.interceptor

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.esteban.ruano.core.data.preferences.dataStore
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_data.constants.Constants.HTTP_HEADER_REQUEST_IS_AUTHORIZABLE_KEY
import com.esteban.ruano.core_data.constants.StatusCodes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()

        val isNotAuthorizable =
            chain.request().headers[HTTP_HEADER_REQUEST_IS_AUTHORIZABLE_KEY] == "false"

        val authToken = runBlocking {
             context.dataStore.data.map{
                it[Preferences.KEY_AUTH_TOKEN] ?: ""
            }.first()
        }

        if(!isNotAuthorizable){
            newRequest.header("Authorization", "Bearer $authToken")

            val response = chain.proceed(newRequest.build())

            if(response.code == StatusCodes.UNAUTHORIZED.code){
                runBlocking {
                    context.dataStore.edit {
                        it[Preferences.KEY_AUTH_TOKEN] = ""
                    }
                }
            }

            return response
        }

        return chain.proceed(newRequest.build())
    }
}