package com.example.corsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.corsa.ui.CorsaNavGraph
import com.example.corsa.ui.theme.CorsaTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CorsaTheme {
                val navController = rememberNavController()

                val appIntentResolver = koinInject<AppIntentResolver>()

                CorsaNavGraph(
                    navController = navController,
                    deepLink = appIntentResolver.resolve(intent)
                )
            }
        }
    }
}

sealed class DeepLink {
    data class SharedRun(val shareToken: String) : DeepLink()

    companion object {
        fun resolve(uri: Uri?): DeepLink? {
            uri ?: return null
            if (uri.scheme != "corsa") return null

            return when (uri.host) {
                "run" -> uri.lastPathSegment?.let { SharedRun(it) }
                else -> null
            }
        }
    }
}

class AppIntentResolver(
    private val supabase: SupabaseClient
) {

    fun resolve(intent: Intent): DeepLink? {
        supabase.handleDeeplinks(
            intent,
            onError = { Log.e("AppIntentResolver", "Error on handling intent")}
        )

        return DeepLink.resolve(intent.data)
    }
}