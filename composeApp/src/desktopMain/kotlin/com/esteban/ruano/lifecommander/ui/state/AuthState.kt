package ui.state
sealed class AuthState {
    data class Unauthenticated(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val errorMessage: String? = null,
        val isLoading: Boolean = false
    ) : AuthState()
    object Authenticated : AuthState()
}