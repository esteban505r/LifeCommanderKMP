package services.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.models.AuthResponse
import services.auth.models.LoginRequest
import services.auth.models.SignUpRequest
import com.esteban.ruano.lifecommander.utils.LOGIN_ENDPOINT
import com.esteban.ruano.lifecommander.utils.SIGNUP_ENDPOINT
import ui.services.auth.AuthRepository

class AuthService(
    private val client: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
):AuthRepository {
    override suspend fun login(email: String, password: String):AuthResponse {
        val response = client.post(LOGIN_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }

        if (response.status == HttpStatusCode.OK) {
            val authResponse = response.body<AuthResponse>()
            tokenStorageImpl.saveToken(authResponse.token)
            return authResponse
        } else {
            throw Exception("Invalid email or password")
        }
    }
    
    override suspend fun signUp(name:String,email: String, password: String) {
        val response = client.post(SIGNUP_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(SignUpRequest(name,email, password))
        }

        if (response.status == HttpStatusCode.Created) {
            return
        } else {
            throw Exception("Failed to create account")
        }
    }
    
    override suspend fun logout() {
        tokenStorageImpl.clearToken()
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return tokenStorageImpl.getToken() != null
    }
} 