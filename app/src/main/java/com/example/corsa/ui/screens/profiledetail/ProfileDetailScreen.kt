package com.example.corsa.ui.screens.profiledetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.corsa.ui.composables.BackTopBar
import com.example.corsa.ui.composables.ProfileStats
import com.example.corsa.ui.composables.UserEntry
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.AppError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    navController: NavController,
    state: ProfileDetailState,
    action: ProfileDetailAction
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when (state.error ) {
            is AppError.Present -> snackbarHostState.showSnackbar(state.error.message)
            else -> {}
        }
    }
    Scaffold(
        topBar = { BackTopBar(navController = navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ProfileStats(
                navController = navController,
                runEntries    = state.runs,
                infoEntries   = state.userEntry,
                header = { ProfileHeader(
                    userInfo      = state.userEntry,
                    isFollowing   = state.isFollowing,
                     onFollowClick = { action.toggleFollow() }
                ) }
            )
        }
    }
}

// ── Profile header ────────────────────────────────────────────────────────

@Composable
fun ProfileHeader(
    userInfo: UserEntry,
    isFollowing: Boolean,
    onFollowClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text     = userInfo.displayName,
                color    = cs.onSurface,
                style    = MaterialTheme.typography.displayMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Spacing.md))
            Box(
                modifier = Modifier
                    .size(Size.l)
                    .clip(CircleShape)
                    .background(
                        if (userInfo.avatarUrl != null) Color.Transparent
                        else cs.secondaryContainer
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (userInfo.avatarUrl != null) {
                    AsyncImage(
                        model = userInfo.avatarUrl,
                        contentDescription = "${userInfo.displayName}'s avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(Size.l)
                            .clip(CircleShape)
                            .background(cs.secondaryContainer),
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(Size.l)
                            .clip(CircleShape)
                            .background(cs.secondaryContainer),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Pulsante segui
        Button(
            onClick = {
                android.util.Log.d("ProfileDetail", "Button clicked")
                onFollowClick()
            },
            shape   = RoundedCornerShape(50),
            colors  = if (isFollowing)
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                )
            else ButtonDefaults.buttonColors(),
            modifier = Modifier
                .padding(horizontal = Spacing.lg)
                .fillMaxWidth()
        ) {
            Text(text = if (isFollowing) "Seguito" else "Segui")
        }
    }
}
// ── Loading / Error states ────────────────────────────────────────────────

@Composable
fun ProfileDetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ProfileDetailError(message: String) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}