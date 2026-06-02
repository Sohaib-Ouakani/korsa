package com.example.corsa.data.repositories


import android.content.Intent
import android.util.Log
import com.example.corsa.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun logout()
    suspend fun getEmail(): String
    suspend fun updatePassword(oldPassword: String, newPassword: String)
    fun isEmailUser(): Boolean
    suspend fun resetPassword(email: String)
    fun handleDeeplinks(
        intent: Intent,
        onSessionSuccess: (UserSession) -> Unit,
        onError: (Throwable) -> Unit
    )
    val sessionStatus: Flow<SessionStatus>
}

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    private var cachedProfile: Profile? = null

    override val sessionStatus: Flow<SessionStatus>
        get() = supabase.auth.sessionStatus

    override suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun register(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun logout() {
        supabase.auth.signOut()
        cachedProfile = null
    }

    override suspend fun getEmail(): String {
        return supabase.auth.currentUserOrNull()?.email
            ?: throw IllegalStateException("No authenticated user")
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String) {
        requireEmailProvider()

        val email = supabase.auth.currentUserOrNull()?.email
            ?: error("User not authenticated")

        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = oldPassword
        }

        supabase.auth.updateUser {
            password = newPassword
        }
    }

    override fun isEmailUser(): Boolean {
        val identities = supabase.auth.currentUserOrNull()?.identities
            ?: return false

        return identities.any { it.provider == "email" }
    }

    override suspend fun resetPassword(email: String) {
        supabase.auth.resetPasswordForEmail(
            email,
            "corsa://reset-password"
        )
    }

    override fun handleDeeplinks(
        intent: Intent,
        onSessionSuccess: (UserSession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        supabase.handleDeeplinks(
            intent,
            onSessionSuccess,
            onError
        )
    }

    private fun requireEmailProvider() {
        val identities = supabase.auth.currentUserOrNull()?.identities
            ?: error("User not authenticated")

        val isEmailUser = identities.any { it.provider == "email" }
        if (!isEmailUser) error("Only email accounts can change email or password")
    }
}