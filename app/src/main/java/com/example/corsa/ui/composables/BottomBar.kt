package com.example.corsa.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.corsa.ui.CorsaRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.corsa.utils.sprint
import com.example.corsa.ui.theme.Size

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination

    BottomAppBar {
        FollowItem(currentRoute, navController)
        RunItem(currentRoute, navController)
        StatsItem(currentRoute, navController)
    }
}

@Composable
private fun RowScope.StatsItem(
    currentRoute: NavDestination?,
    navController: NavController
) {
    val selected = currentRoute?.hasRoute<CorsaRoute.StatsScreen>() == true

    NavigationBarItem(
        selected = selected,
        onClick = { navController.navigate(CorsaRoute.StatsScreen) },
        icon = {
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                modifier = Modifier.animatedIconModifier(selected, Size.m),
            )
        },
        label = {
            Text(
                "PROFILE",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        },
    )
}

@Composable
private fun RowScope.RunItem(
    currentRoute: NavDestination?,
    navController: NavController
) {
    val selected = currentRoute?.hasRoute<CorsaRoute.Home>() == true

    NavigationBarItem(
        selected = selected,
        onClick = { navController.navigate(CorsaRoute.Home) },
        icon = {
            Icon(
                imageVector = sprint,
                contentDescription = null,
                modifier = Modifier.animatedIconModifier(selected, Size.m),
            )
        },
        label = {
            Text(
                text = "RUN",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        },
    )
}

@Composable
private fun RowScope.FollowItem(
    currentRoute: NavDestination?,
    navController: NavController
) {
    val selected = currentRoute?.hasRoute<CorsaRoute.FollowScreen>() == true

    NavigationBarItem(
        selected = selected,
        onClick = { navController.navigate(CorsaRoute.FollowScreen) },
        icon = {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = null,
                modifier = Modifier.animatedIconModifier(selected, Size.m),
            )
        },
        label = {
            Text(
                "FOLLOW",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        },
    )
}

@Composable
fun Modifier.animatedIconModifier(selected: Boolean, size: Dp): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.3f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "iconScale"
    )
    return this
        .size(size)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}