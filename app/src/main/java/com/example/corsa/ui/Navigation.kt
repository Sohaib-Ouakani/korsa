package com.example.corsa.ui

import androidx.compose.runtime.Composable
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
import com.example.corsa.ui.screens.home.StopWatchScreen
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

sealed interface CorsaRoute {
    @Serializable data object Home : CorsaRoute
    @Serializable data object AddFollowScreen : CorsaRoute
    @Serializable data object StopWatchScreen : CorsaRoute
    @Serializable data object StatsScreen : CorsaRoute

    @Serializable data object FollowScreen : CorsaRoute
    @Serializable data object AuthScreen : CorsaRoute
    @Serializable data object LoginScreen : CorsaRoute
    @Serializable data object RegisterScreen : CorsaRoute
    @Serializable data object SettingsScreen : CorsaRoute
    @Serializable data class ProfileDetailScreen(val userId: String) : CorsaRoute

    @Serializable data class RunDetailScreen(val runId: String) : CorsaRoute
}

@Composable
fun CorsaNavGraph(navController: NavHostController) {
    val sessionViewModel = koinViewModel<SessionViewModel>()
    val startDestination by sessionViewModel.startDestination.collectAsStateWithLifecycle()

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
                        onEmailLogin = authViewModel::loginWithEmail
                    )
                }
                composable<CorsaRoute.RegisterScreen> {
                    val authViewModel = koinViewModel<AuthViewModel>()
                    val state by authViewModel.authState.collectAsStateWithLifecycle()
                    RegisterScreen(
                        navController = navController,
                        state = state,
                        onEmailRegister = authViewModel::registerWithEmail
                    )
                }
                composable<CorsaRoute.Home> {
                    val homeViewModel = koinViewModel<HomeViewModel>()
                    val state by homeViewModel.state.collectAsStateWithLifecycle()
                    HomeScreen(state, navController, homeViewModel)
                }
                composable<CorsaRoute.StopWatchScreen> {
                    val homeViewModel = koinViewModel<HomeViewModel>()
                    val timerState by homeViewModel.timerState.collectAsStateWithLifecycle()
                    StopWatchScreen(
                        timerState,
                        navController,
                        homeViewModel.stopWatchActions,
                        homeViewModel,
                        onRunSaved = {
                            navController.navigate(CorsaRoute.Home)
                        }
                    )
                }
                composable<CorsaRoute.StatsScreen> {
                    val statsViewModel = koinViewModel<StatsScreenViewModel>()
                    val userEntry by statsViewModel.profile.collectAsStateWithLifecycle()
                    userEntry?.let { StatsScreen(navController, statsViewModel ) }
                }
                composable<CorsaRoute.FollowScreen> {
                    val friendsVM = koinViewModel<FollowingViewModel>()
                    FollowScreen(navController = navController, friendsVM)
                }
                composable<CorsaRoute.SettingsScreen> {
                    val settingsViewModel = koinViewModel<SettingsViewModel>()
                    val settingsInfo by settingsViewModel.settingsInfo.collectAsStateWithLifecycle()
                    val state by settingsViewModel.settingsState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        navController = navController,
                        settingsInfo = settingsInfo,
                        state = state,
                        onLogOut = settingsViewModel::logout,
                        onSaveNewUsername = settingsViewModel::saveNewUsername,
                        onSaveNewPassword = settingsViewModel::saveNewPassword,
                        onClearError = settingsViewModel::clearError,
                        onUploadAvatar = settingsViewModel::uploadAvatar,
                    )
                }
                composable<CorsaRoute.RunDetailScreen> {
                    val runDetailViewModel = koinViewModel<RunDetailViewModel>()
                    val state by runDetailViewModel.runDetailState.collectAsStateWithLifecycle()
                    RunDetailScreen(
                        navController = navController,
                        state = state,
                        toggleLike = runDetailViewModel::toggleLike,
                        onAddComment = runDetailViewModel::onAddComment
                    )
                }
                composable<CorsaRoute.ProfileDetailScreen> {
                    val viewModel = koinViewModel<ProfileDetailViewModel>()
                    ProfileDetailScreen(navController = navController, viewModel = viewModel)
                }
                composable<CorsaRoute.AddFollowScreen> {
                    val friendsVM = koinViewModel<FollowingViewModel>()
                    AddFollowScreen(navController, friendsVM)
                }
            }
        }
    }
}