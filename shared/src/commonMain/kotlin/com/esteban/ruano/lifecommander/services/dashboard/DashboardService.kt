package services.dashboard

import com.esteban.ruano.lifecommander.utils.appHeaders
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.models.dashboard.DashboardResponse
import encodeUrlWithSpaces
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.TimeZone
import services.auth.TokenStorageImpl

class DashboardService(
    private val baseUrl: String,
    private val tokenStorageImpl: TokenStorageImpl,
    private val httpClient: HttpClient
) {
    suspend fun getDashboardData(): DashboardResponse {
        try{
            val parameters = Parameters.build {
                append("dateTime", getCurrentDateTime(
                    TimeZone.currentSystemDefault()
                ).formatDefault())
            }
            val encodedUrl = encodeUrlWithSpaces("${baseUrl}/dashboard", parameters)
            val response: HttpResponse = httpClient.get(encodedUrl) {
                appHeaders(tokenStorageImpl.getToken())
            }
            if (response.status.isSuccess()) {
                val response = response.body<DashboardResponse>()
                return response
            } else {
                throw Exception("Failed to fetch dashboard data: ${response.status.description}")
            }
        }
        catch (e: Exception) {
            throw Exception("Error fetching dashboard data: ${e.message}", e)
        }
    }
}

