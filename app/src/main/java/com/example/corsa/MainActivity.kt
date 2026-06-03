package com.example.corsa

import android.os.Bundle
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

                val supabase = koinInject<SupabaseClient>()
                supabase.handleDeeplinks(intent)

                CorsaNavGraph(
                    navController = navController,
                    deepLinkUri = intent?.data?.toString()
                )
            }
        }
    }
}