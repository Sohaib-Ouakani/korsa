package com.example.corsa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.corsa.ui.screens.auth.AuthScreen
import com.example.corsa.ui.screens.auth.LoginScreen
import com.example.corsa.ui.screens.auth.RegisterScreen
import com.example.corsa.ui.screens.home.HomeScreen
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.logintester.LoginScreen
import com.example.corsa.ui.screens.stats.StatsScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface CorsaRoute {
    @Serializable data object LoginTester : CorsaRoute
    @Serializable data object Home : CorsaRoute
    @Serializable data object StatsScreen : CorsaRoute
    @Serializable data object AuthScreen : CorsaRoute
    @Serializable data object LoginScreen : CorsaRoute
    @Serializable data object RegisterScreen : CorsaRoute
}

@Composable
fun CorsaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = CorsaRoute.Home
    ) {
        composable<CorsaRoute.LoginTester> {
            LoginScreen(navController = navController) { }
        }
        composable<CorsaRoute.Home> {
            val state = HomeViewModel.state
            HomeScreen(state, navController)
        }
        composable<CorsaRoute.StatsScreen> {
            StatsScreen(navController = navController)
        }
        composable<CorsaRoute.AuthScreen> {
            AuthScreen(navController = navController)
        }
        composable<CorsaRoute.LoginScreen> {
            LoginScreen(navController = navController)
        }
        composable<CorsaRoute.RegisterScreen> {
            RegisterScreen(navController = navController)
        }
    }
}