package com.example.corsa.data.repositories

import com.example.corsa.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.auth.providers.builtin.Email

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>


    val sessionStatus: Flow<SessionStatus>
}

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    private var cachedProfile: Profile? = null

    override val sessionStatus: Flow<SessionStatus>
        get() = supabase.auth.sessionStatus

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabase.auth.signOut()
        cachedProfile = null
    }
}