package com.example.corsa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.corsa.ui.screens.SessionViewModel
import com.example.corsa.ui.screens.StartDestination
import com.example.corsa.ui.screens.friends.FollowScreen
import com.example.corsa.ui.screens.auth.AuthScreen
import com.example.corsa.ui.screens.auth.AuthViewModel
import com.example.corsa.ui.screens.auth.LoginScreen
import com.example.corsa.ui.screens.auth.RegisterScreen
import com.example.corsa.ui.screens.friends.AddFollowScreen
import com.example.corsa.ui.screens.friends.FollowingViewModel
import com.example.corsa.ui.screens.home.HomeScreen
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.settings.SettingsScreen
import com.example.corsa.ui.screens.settings.SettingsViewModel
import com.example.corsa.ui.screens.profiledetail.ProfileDetailScreen
import com.example.corsa.ui.screens.profiledetail.ProfileDetailViewModel
import com.example.corsa.ui.screens.rundetail.RunDetailScreen
import com.example.corsa.ui.screens.rundetail.RunDetailViewModel
import com.example.corsa.ui.screens.splash.SplashScreen
import com.example.corsa.ui.screens.stats.StatsScreen
import com.example.corsa.ui.screens.stats.StatsScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import com.example.corsa.ui.screens.home.run.RunViewModel
import com.example.corsa.ui.screens.home.run.StopWatchScreen

sealed interface CorsaRoute {
    @Serializable data object Home : CorsaRoute
    @Serializable data object AddFollowScreen : CorsaRoute
    @Serializable data object RunScreen : CorsaRoute
    @Serializable data object StatsScreen : CorsaRoute

    @Serializable data object FollowScreen : CorsaRoute
    @Serializable data object AuthScreen : CorsaRoute
    @Serializable data object LoginScreen : CorsaRoute
    @Serializable data object RegisterScreen : CorsaRoute
    @Serializable data object SettingsScreen : CorsaRoute
    @Serializable data class ProfileDetailScreen(val userId: String) : CorsaRoute

    @Serializable data class RunDetailScreen(val runId: String) : CorsaRoute
    @Serializable data class SharedRunScreen(val shareToken: String) : CorsaRoute  // ← add this
}

@Composable
fun CorsaNavGraph(
    navController: NavHostController,
    deepLinkUri: String? = null
) {
    val sessionViewModel = koinViewModel<SessionViewModel>()
    val startDestination by sessionViewModel.startDestination.collectAsStateWithLifecycle()

    LaunchedEffect(deepLinkUri) {
        if (deepLinkUri != null) {
            val uri = deepLinkUri.toUri()
            if (uri.scheme == "corsa" && uri.host == "run") {
                val token = uri.lastPathSegment
                if (token != null) {
                    navController.navigate(CorsaRoute.SharedRunScreen(token))
                }
            }
        }
    }

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
                    val authViewModel = koinViewModel<AuthViewModel>()
                    val state by authViewModel.authState.collectAsStateWithLifecycle()
                    LoginScreen(
                        navController = navController,
                        state = state,
                        authActions = authViewModel.authActions,
                    )
                }
                composable<CorsaRoute.RegisterScreen> {
                    val authViewModel = koinViewModel<AuthViewModel>()
                    val state by authViewModel.authState.collectAsStateWithLifecycle()
                    RegisterScreen(
                        navController = navController,
                        state = state,
                        authActions = authViewModel.authActions
                    )
                }
                composable<CorsaRoute.Home> {
                    val homeViewModel = koinViewModel<HomeViewModel>()
                    val state by homeViewModel.state.collectAsStateWithLifecycle()
                    HomeScreen(state, navController)
                }
                composable<CorsaRoute.RunScreen> {
                    val runViewModel = koinViewModel<RunViewModel>()
                    val state by runViewModel.uiState.collectAsStateWithLifecycle()
                    val actions = runViewModel.runActions
                    StopWatchScreen(
                        state,
                        navController,
                        actions = actions,
                    )
                }
                composable<CorsaRoute.StatsScreen> {
                    val statsViewModel = koinViewModel<StatsScreenViewModel>()
                    val state by statsViewModel.statsState.collectAsStateWithLifecycle()
                    StatsScreen(
                        navController = navController,
                        state = state,
                        actions =  statsViewModel.statsActions,
                    )
                }
                composable<CorsaRoute.FollowScreen> {
                    val friendsVM = koinViewModel<FollowingViewModel>()
                    val followState by friendsVM.followState.collectAsStateWithLifecycle()
                    val searchState by friendsVM.searchState.collectAsStateWithLifecycle()
                    FollowScreen(navController = navController, followState, searchState, friendsVM.followAction)
                }
                composable<CorsaRoute.SettingsScreen> {
                    val settingsViewModel = koinViewModel<SettingsViewModel>()
                    val state by settingsViewModel.settingsState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        navController = navController,
                        state = state,
                        actions = settingsViewModel.settingsActions,
                    )
                }
                composable<CorsaRoute.RunDetailScreen> {
                    val runDetailViewModel = koinViewModel<RunDetailViewModel>()
                    val state by runDetailViewModel.runDetailState.collectAsStateWithLifecycle()
                    RunDetailScreen(
                        navController = navController,
                        state = state,
                        actions = runDetailViewModel.runDetailActions
                    )
                }
                composable<CorsaRoute.SharedRunScreen> {
                    val runDetailViewModel = koinViewModel<RunDetailViewModel>()
                    val state by runDetailViewModel.runDetailState.collectAsStateWithLifecycle()
                    RunDetailScreen(
                        navController = navController,
                        state = state,
                        actions = runDetailViewModel.runDetailActions
                    )
                }
                composable<CorsaRoute.ProfileDetailScreen> {
                    val viewModel = koinViewModel<ProfileDetailViewModel>()
                    ProfileDetailScreen(navController = navController, viewModel = viewModel)
                }
                composable<CorsaRoute.AddFollowScreen> {
                    val friendsVM = koinViewModel<FollowingViewModel>()
                    val searchState by friendsVM.searchState.collectAsStateWithLifecycle()
                    AddFollowScreen(navController, searchState, friendsVM.followAction)
                }
            }
        }
    }
}