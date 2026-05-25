package com.example.corsa.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.corsa.ui.screens.home.HomeScreen
import com.example.corsa.ui.screens.logintester.LoginScreen
import kotlinx.serialization.Serializable

sealed interface CorsaRoute {
    @Serializable data object LoginTester : CorsaRoute
    @Serializable data object Home : CorsaRoute
}

@Composable
fun CorsaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = CorsaRoute.Home
    ) {
        composable<CorsaRoute.LoginTester> {
            LoginScreen { }
        }

        composable<CorsaRoute.Home> {
            HomeScreen(navController = navController)
        }
    }
}