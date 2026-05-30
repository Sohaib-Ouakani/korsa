package com.example.corsa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing

@Composable
fun StopWatchScreen(
    status: StopWatchStatus,
    navController: NavController,
    actions: StopWatchAction,
    viewModel: HomeViewModel,
    onRunSaved: () -> Unit
){
    val cs = MaterialTheme.colorScheme
    val text = if (status.isRunning) "GO!" else "READY?"
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val runState   by viewModel.runState.collectAsStateWithLifecycle()
    val saveState  by viewModel.saveState.collectAsStateWithLifecycle()

    // Navigate home automatically when save succeeds
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) onRunSaved()
    }

    Column(
        modifier = Modifier
            .background(cs.primary)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            color = cs.onPrimary,
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(Spacing.lg))
        if (saveState is SaveState.Error) {
            Text(
                text  = (saveState as SaveState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
        // Distance
        Text(
            text  = "${"%.2f".format(runState.distanceKm)} km",
            style = MaterialTheme.typography.displayMedium
        )
        // Pace
        Text(
            text  = runState.formattedPace,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = status.formattedTime,
            color = cs.onPrimary,
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(Modifier.height(Spacing.lg))
        if (status.isRunning) {
            IconButton(
                onClick = { actions.pause() },
                modifier = Modifier.size(Size.xl)
            ) {
                Icon(
                    imageVector = Icons.Filled.PauseCircle,
                    contentDescription = "Pause",
                    tint = cs.onPrimary,
                    modifier = Modifier.size(Size.xl)
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxxl),
            ) {
                IconButton(
                    onClick = {
                        actions.stop()
                        navController.navigate(CorsaRoute.Home)
                    },
                    enabled  = saveState !is SaveState.Saving,
                    modifier = Modifier.size(Size.xl)
                ) {
                    Icon(
                        imageVector = Icons.Filled.StopCircle,
                        contentDescription = "Stop",
                        tint = cs.onPrimary,
                        modifier = Modifier.size(Size.xl)
                    )
                }
                IconButton(
                    onClick = { actions.start() },
                    modifier = Modifier.size(Size.xl)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Pause",
                        tint = cs.onPrimary,
                        modifier = Modifier.size(Size.xl)
                    )
                }
            }
        }
    }
}