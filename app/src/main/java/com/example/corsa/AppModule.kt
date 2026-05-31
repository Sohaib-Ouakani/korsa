package com.example.corsa

import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.AuthRepositoryImpl
import com.example.corsa.data.repositories.RunsRepositoryImpl
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.ProfilesRepositoryImpl
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.screens.friends.FollowingViewModel
import com.example.corsa.ui.screens.SessionViewModel
import com.example.corsa.ui.screens.auth.AuthViewModel
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.settings.SettingsViewModel
import com.example.corsa.ui.screens.profiledetail.ProfileDetailViewModel
import com.example.corsa.ui.screens.rundetail.RunDetailViewModel
import com.example.corsa.ui.screens.stats.StatsScreenViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
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
            install(Postgrest)
            install(Realtime)
        }
    }

    single<ProfilesRepository> { ProfilesRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<RunsRepository> { RunsRepositoryImpl(get()) }
    single<LocationProvider> { LocationProvider(get()) }

    viewModel { SessionViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { StatsScreenViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { FollowingViewModel(get(), get() ) }
    viewModel { params -> RunDetailViewModel(get(), params.get()) }
    viewModel { ProfileDetailViewModel(get(), get(), get()) }
}