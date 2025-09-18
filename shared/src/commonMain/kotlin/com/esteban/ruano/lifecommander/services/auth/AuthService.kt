package services.auth

import com.esteban.ruano.lifecommander.models.ForgotPasswordRequest
import com.esteban.ruano.lifecommander.models.ResetPasswordRequest
import com.esteban.ruano.lifecommander.models.ResetVerifyResponse
import com.esteban.ruano.lifecommander.models.ResetWithSessionRequest
import com.esteban.ruano.lifecommander.models.VerifyPinRequest
import com.esteban.ruano.lifecommander.models.VerifyTokenRequest
import com.esteban.ruano.lifecommander.utils.FORGOT_PASSWORD_ENDPOINT
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.models.AuthResponse
import services.auth.models.LoginRequest
import services.auth.models.SignUpRequest
import com.esteban.ruano.lifecommander.utils.LOGIN_ENDPOINT
import com.esteban.ruano.lifecommander.utils.RESET_PASSWORD_ENDPOINT
import com.esteban.ruano.lifecommander.utils.SIGNUP_ENDPOINT
import com.esteban.ruano.lifecommander.utils.VERIFY_RESET_PIN_ENDPOINT
import ui.services.auth.AuthRepository

class AuthService(
    private val client: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl? = null
):AuthRepository {
    override suspend fun login(email: String, password: String):AuthResponse {
        val response = client.post(LOGIN_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }

        if (response.status == HttpStatusCode.OK) {
            val authResponse = response.body<AuthResponse>()
            tokenStorageImpl?.saveToken(authResponse.token)
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
        tokenStorageImpl?.clearToken()
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return tokenStorageImpl?.getToken() != null
    }

    override suspend fun forgotPassword(email: String) {
        val response = client.post(FORGOT_PASSWORD_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(ForgotPasswordRequest(email))
        }

        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to request password reset")
        }
    }

    override suspend fun resetPasswordWithSession(resetToken: String, newPassword: String) {
        val response = client.post(RESET_PASSWORD_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(ResetWithSessionRequest(resetToken = resetToken, newPassword = newPassword))
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to reset password")
        }
    }

    override suspend fun verifyResetPin(email: String, pin: String): String {
        val response = client.post(VERIFY_RESET_PIN_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(VerifyPinRequest(email = email, pin = pin))
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Invalid or expired code")
        }
        // The server returns { "reset_token": "<opaque>" }
        val body = response.body<ResetVerifyResponse>()
        return body.reset_token
    }
} 