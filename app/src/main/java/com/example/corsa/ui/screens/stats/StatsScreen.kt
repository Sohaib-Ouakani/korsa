package com.example.corsa.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.ProfileStats
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.composables.UserEntry
import com.example.corsa.ui.screens.splash.SplashScreen
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing


@Composable
fun StatsScreen(
    navController: NavController,
    state: StatsState,
    actions: StatsActions
) {

    LaunchedEffect(Unit) {
        actions.refreshProfile()  // re-fetches every time the screen enters composition
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error)
        }
    }
    val cs = MaterialTheme.colorScheme


    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            if (!state.isLoading) {
                ProfileStats(
                    navController,
                    state.runs,
                    state.profile,
                    { ProfileHeader(state.profile, cs) }
                )
            }
            else{
                SplashScreen()
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    infoEntries: UserEntry,
    cs: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, // nome a sx, avatar a dx
    ) {
        Text(
            text = infoEntries.displayName,
            color = cs.onSurface,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.weight(1f), // occupa lo spazio senza spingere l'avatar
        )
        Spacer(Modifier.width(Spacing.md))
        Box(
            modifier = Modifier
                .size(Size.l)
                .clip(CircleShape)
                .background(cs.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = infoEntries.displayName.first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}