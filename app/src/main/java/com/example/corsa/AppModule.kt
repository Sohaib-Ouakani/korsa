package com.example.corsa

import android.content.Context
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.remote.LocationInfoRemote
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.AuthRepositoryImpl
import com.example.corsa.data.repositories.RunsRepositoryImpl
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.ProfilesRepositoryImpl
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.screens.friends.FollowingViewModel
import com.example.corsa.ui.screens.SessionViewModel
import com.example.corsa.ui.screens.auth.LoginViewModel
import com.example.corsa.ui.screens.auth.RegisterViewModel
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.home.run.RunViewModel
import com.example.corsa.ui.screens.settings.SettingsViewModel
import com.example.corsa.ui.screens.profiledetail.ProfileDetailViewModel
import com.example.corsa.ui.screens.resetpassword.ResetPasswordViewModel
import com.example.corsa.ui.screens.rundetail.RunDetailViewModel
import com.example.corsa.ui.screens.stats.StatsScreenViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                scheme = "corsa"
                host = "reset-password"
            }
            install(ComposeAuth) {
                googleNativeLogin(BuildConfig.GOOGLE_CLIENT_ID)
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }

    }
    single { get<SupabaseClient>().composeAuth }
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single<AppIntentResolver> { AppIntentResolver(get()) }

    single<ProfilesRepository> { ProfilesRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<RunsRepository> { RunsRepositoryImpl(get(), get()) }
    single<LocationProvider> { LocationProvider(get()) }
    single<LocationInfoRemote>{ LocationInfoRemote(get()) }

    viewModel { SessionViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { StatsScreenViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { RunViewModel(get(), get(), get<Context>().applicationContext) }
    viewModel { FollowingViewModel(get(), get(),) }
    viewModel { params -> RunDetailViewModel(get(), get(),params.get()) }
    viewModel { ProfileDetailViewModel(get(), get(), get()) }
    viewModel { ResetPasswordViewModel(get()) }
}