package com.example.corsa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.corsa.ui.screens.AuthStateViewModel
import com.example.corsa.ui.screens.StartDestination
import com.example.corsa.ui.screens.friends.FriendsScreen
import com.example.corsa.ui.screens.auth.AuthScreen
import com.example.corsa.ui.screens.auth.LoginScreen
import com.example.corsa.ui.screens.auth.RegisterScreen
import com.example.corsa.ui.screens.home.HomeScreen
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.home.StopWatchScreen
import com.example.corsa.ui.screens.profile.ProfileScreen
import com.example.corsa.ui.screens.profile.ProfileViewModel
import com.example.corsa.ui.screens.rundetail.RunDetailScreen
import com.example.corsa.ui.screens.rundetail.RunDetailViewModel
import com.example.corsa.ui.screens.splash.SplashScreen
import com.example.corsa.ui.screens.stats.StatsScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface CorsaRoute {
    @Serializable data object Home : CorsaRoute
    @Serializable data object StopWatchScreen : CorsaRoute
    @Serializable data object StatsScreen : CorsaRoute

    @Serializable data object FriendsScreen : CorsaRoute
    @Serializable data object AuthScreen : CorsaRoute
    @Serializable data object LoginScreen : CorsaRoute
    @Serializable data object RegisterScreen : CorsaRoute
    @Serializable data object ProfileScreen : CorsaRoute
    @Serializable data class RunDetailScreen(val runId: String) : CorsaRoute
}

@Composable
fun CorsaNavGraph(navController: NavHostController) {
    val authViewModel: AuthStateViewModel = koinViewModel()
    val startDestination by authViewModel.startDestination.collectAsStateWithLifecycle()

    when (startDestination) {
        StartDestination.Loading -> SplashScreen()
        else -> {
            NavHost(
                navController = navController,
                startDestination = when (startDestination) {
                    StartDestination.Home -> CorsaRoute.Home
                    else -> CorsaRoute.AuthScreen
                }
            ) {
                composable<CorsaRoute.AuthScreen> {
                    AuthScreen(navController = navController)
                }
                composable<CorsaRoute.LoginScreen> {
                    LoginScreen(
                        navController = navController,
                        onEmailLogin = { string: String, string1: String -> }
                    )
                }
                composable<CorsaRoute.RegisterScreen> {
                    RegisterScreen(navController = navController)
                }
                composable<CorsaRoute.Home> {
                    val homeViewModel = koinViewModel<HomeViewModel>()
                    val state = homeViewModel.state
                    HomeScreen(state, navController)
                }
                composable<CorsaRoute.StopWatchScreen> {
                    val homeViewModel = koinViewModel<HomeViewModel>()
                    val timerState by homeViewModel.timerState.collectAsStateWithLifecycle()
                    StopWatchScreen(timerState, navController, homeViewModel.stopWatchActions)
                }
                composable<CorsaRoute.StatsScreen> {
                    StatsScreen(navController = navController)
                }
                composable<CorsaRoute.FriendsScreen> {
                    FriendsScreen(navController = navController)
                }
                composable<CorsaRoute.ProfileScreen> {
                    val profileViewModel = koinViewModel<ProfileViewModel>()
                    ProfileScreen(navController = navController, profileViewModel::logout)
                }
                composable<CorsaRoute.RunDetailScreen> {
                    val viewModel = koinViewModel<RunDetailViewModel>()
                    RunDetailScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}