package com.esteban.ruano.test_core.server

import okhttp3.mockwebserver.MockResponse
import java.net.HttpURLConnection

object TestServerUtils {
    fun mockResponse(responseBody: String, status: Int = HttpURLConnection.HTTP_OK) =
        MockResponse()
            .setResponseCode(status)
            .setBody(responseBody)
}