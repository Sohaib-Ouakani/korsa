package com.example.corsa

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

val supabase = createSupabaseClient(
    supabaseUrl = "https://gjqtujwgmdiyprviweyw.supabase.co",
    supabaseKey = "sb_publishable_enahpShUve1sgzf5uwBOHw_8mUt6oOW"
) {
    install(Auth)
    install(ComposeAuth) {
        googleNativeLogin("911076877881-m0sgfl17faeu7fpcsh2nf722uoiup8q5.apps.googleusercontent.com") // ⚠️ Web Client ID, NOT the Android one
    }
}