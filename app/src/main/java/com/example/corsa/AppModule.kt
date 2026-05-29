package com.example.corsa

import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.AuthRepositoryImpl
import com.example.corsa.data.repositories.FakeRunsRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.screens.AuthStateViewModel
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.rundetail.RunDetailViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(ComposeAuth) {
                googleNativeLogin(BuildConfig.GOOGLE_CLIENT_ID)
            }
        }
    }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<RunsRepository> { FakeRunsRepository() }

    viewModel { AuthStateViewModel(get()) }
    viewModel { HomeViewModel() }
    viewModel { params -> RunDetailViewModel(get(), params.get()) }
}