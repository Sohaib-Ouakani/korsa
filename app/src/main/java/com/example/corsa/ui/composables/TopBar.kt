package com.example.corsa.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.corsa.ui.CorsaRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    avatarUrl: String? = null
) {
    CenterAlignedTopAppBar(
        title = { AppBarText() },
        actions = {
            IconButton(
                onClick = { navController.navigate(CorsaRoute.SettingsScreen) },
                modifier = Modifier
                    .size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            ) {
                AvatarIcon(avatarUrl)
            }
        }
    )
}

@Composable
private fun AvatarIcon(avatarUrl: String?) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(22.dp),
        )
    }
}

