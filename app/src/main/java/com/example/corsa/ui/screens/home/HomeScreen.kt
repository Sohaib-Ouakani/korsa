package com.example.corsa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.corsa.data.remote.LocationInfo
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.premissions.LocationPermissionHandler
import com.example.corsa.ui.premissions.LocationPermissionState
import com.example.corsa.ui.screens.splash.SplashScreen
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.AppError

@Composable
fun HomeScreen(
    state: HomeState,
    actions: HomeActions,
    navController: NavController
) {
    val cs = MaterialTheme.colorScheme

    LocationPermissionHandler { permissionState, requestPermission ->
        when (permissionState) {
            LocationPermissionState.GRANTED -> {
                LaunchedEffect(Unit) {
                    actions.fetchLocationInfo()
                }
                Content(cs, navController, state)
            }

            LocationPermissionState.DENIED -> {
                PermissionRationaleScreen(onRequest = requestPermission)
            }

            LocationPermissionState.PERMANENTLY_DENIED -> {
                //send to Settings
                PermissionDeniedScreen()
            }
        }
    }
}

@Composable
private fun Content(
    cs: ColorScheme,
    navController: NavController,
    state: HomeState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state) {
        when(state.appError) {
            is AppError.Present -> {
                snackbarHostState.showSnackbar(state.appError.message)
            }
            else -> {}
        }
    }
    Scaffold(
        topBar = { TopBar(navController, state.myProfileUrl) },
        bottomBar = { BottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            SplashScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(Spacing.md))

                LocationLabel(cs, state.locationInfo)

                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = "READY TO\nMOVE?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    color = cs.onSurface,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Spacing.xxl))

                StartButton(cs) { navController.navigate(CorsaRoute.RunScreen) }

                Spacer(Modifier.height(Spacing.xxl))

                GoalCard(
                    cs,
                    state.goalKm,
                    state.currentKm,
                    state.progress
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.StartButton(
    cs: ColorScheme,
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(Size.xxl)
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .background(cs.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStartClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start",
                tint = cs.onPrimary,
                modifier = Modifier.size(Size.l),
            )
            Text(
                text = "START",
                style = MaterialTheme.typography.titleMedium,
                color = cs.onPrimary,
            )
        }
    }
}

@Composable
private fun LocationLabel(cs: ColorScheme, locationInfo: LocationInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (locationInfo.cityName != null){
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(Size.s)
        )
        Spacer(Modifier.width(Spacing.sm))
            Text(
                text = locationInfo.cityName,
                color = cs.primary,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(Spacing.sm))
        Icon(
            imageVector = locationInfo.weatherCode.icon,
            contentDescription = "Weather",
            modifier = Modifier.size(Size.s)
        )
        } else {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Size.s),
                strokeWidth = 1.dp,
            )
        }
    }
}

@Composable
private fun GoalCard(
    cs: ColorScheme,
    goalKm: Float,
    currentKm: Float,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.cardSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "GOAL",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.2f KM".format(goalKm),
                        color = cs.onSurface,
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { progress },

                )
                Spacer(
                    modifier = Modifier.height(Spacing.sm)
                )
                Text(
                    text = "%.2f KM".format(currentKm),
                    color = cs.primary,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
private fun PermissionRationaleScreen(onRequest: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Korsa needs location access\nto track your runs.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = cs.onBackground
        )
        Spacer(Modifier.height(Spacing.lg))
        Button(onClick = onRequest) {
            Text("Grant permission")
        }
    }
}

@Composable
private fun PermissionDeniedScreen() {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Location permission was denied.\nEnable it in Settings to use Corsa.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = cs.onBackground
        )
        Spacer(Modifier.height(Spacing.lg))
        Button(onClick = {
            // Opens the app's system settings page
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
            context.startActivity(intent)
        }) {
            Text("Open Settings")
        }
    }
}