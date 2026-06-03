package com.example.corsa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.corsa.DeepLink
import com.example.corsa.ui.CorsaRoute.*
import com.example.corsa.ui.screens.SessionViewModel
import com.example.corsa.ui.screens.AppSessionStatus
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
    @Serializable data class SharedRunScreen(val shareToken: String) : CorsaRoute
    @Serializable data object PasswordResetScreen : CorsaRoute
}

@Composable
fun CorsaNavGraph(
    navController: NavHostController,
    deepLink: DeepLink? = null
) {
    val sessionViewModel = koinViewModel<SessionViewModel>()
    val appSessionStatus by sessionViewModel.appSessionStatus.collectAsStateWithLifecycle()

    var origin by remember(deepLink) { mutableStateOf(deepLink) }

    when (appSessionStatus) {
        AppSessionStatus.Loading -> SplashScreen()
        AppSessionStatus.NotAuthenticated -> {
            NavHost(
                navController = navController,
                startDestination = CorsaRoute.AuthScreen
            ) {
                composable<CorsaRoute.AuthScreen> {
                    AuthScreen(
                        navController = navController,
                        redirectedFromRunDeepLink = (origin is DeepLink.SharedRun)
                    )
                }
                composable<CorsaRoute.LoginScreen> {
                    val authViewModel = koinViewModel<AuthViewModel>()
                    val state by authViewModel.authState.collectAsStateWithLifecycle()
                    LoginScreen(
                        navController = navController,
                        state = state,
                        actions = authViewModel.authActions,
                    )
                }
                composable<CorsaRoute.RegisterScreen> {
                    val authViewModel = koinViewModel<AuthViewModel>()
                    val state by authViewModel.authState.collectAsStateWithLifecycle()
                    RegisterScreen(
                        navController = navController,
                        state = state,
                        actions = authViewModel.authActions
                    )
                }
            }
        }
        AppSessionStatus.Authenticated -> {
            NavHost(
                navController = navController,
                startDestination = Home
            ) {
                composable<Home> {
                    LaunchedEffect(origin) {
                        when(val deepLink = origin) {
                            DeepLink.ResetPassword -> {
                                origin = null
                                navController.navigate(PasswordResetScreen)
                            }
                            is DeepLink.SharedRun -> {
                                origin = null
                                navController.navigate(SharedRunScreen(deepLink.shareToken))
                            }
                            null -> { }
                        }
                    }

                    val homeViewModel = koinViewModel<HomeViewModel>()
                    val state by homeViewModel.state.collectAsStateWithLifecycle()
                    HomeScreen(
                        state,
                        navController,
                    )
                }
                composable<RunScreen> {
                    val runViewModel = koinViewModel<RunViewModel>()
                    val state by runViewModel.uiState.collectAsStateWithLifecycle()
                    val actions = runViewModel.runActions
                    StopWatchScreen(
                        state,
                        navController,
                        actions = actions,
                    )
                }
                composable<StatsScreen> {
                    val statsViewModel = koinViewModel<StatsScreenViewModel>()
                    val state by statsViewModel.statsState.collectAsStateWithLifecycle()
                    StatsScreen(
                        navController = navController,
                        state = state,
                        actions = statsViewModel.statsActions,
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
                composable<SharedRunScreen> {
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
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    ProfileDetailScreen(navController = navController, state, viewModel.profileDetailAction)
                }
                composable<CorsaRoute.AddFollowScreen> {
                    val friendsVM = koinViewModel<FollowingViewModel>()
                    val searchState by friendsVM.searchState.collectAsStateWithLifecycle()
                    AddFollowScreen(navController, searchState, friendsVM.followAction)
                }
                composable<CorsaRoute.PasswordResetScreen> {
                    SplashScreen()
                }
            }
        }
    }
}
