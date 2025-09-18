package ui.services.auth

import services.auth.models.AuthResponse
import services.tasks.models.TaskRequest
import services.tasks.models.TaskResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun signUp(name:String,email: String, password: String): Unit
    suspend fun logout()
    suspend fun isAuthenticated(): Boolean

    suspend fun forgotPassword(email: String)

    suspend fun resetPasswordWithSession(resetToken: String, newPassword: String)
    suspend fun verifyResetPin(email: String,pin: String): String
}