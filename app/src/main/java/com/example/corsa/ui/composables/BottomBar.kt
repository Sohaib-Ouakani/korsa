package com.example.corsa.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.corsa.ui.CorsaRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.corsa.ui.theme.Size

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination

    BottomAppBar() {
        // FRIENDS
        NavigationBarItem(
            selected = currentRoute?.hasRoute<CorsaRoute.FriendsScreen>() == true,
            onClick = { navController.navigate(CorsaRoute.FriendsScreen) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(Size.m),
                )
            },
            label = {
                Text(
                    "FRIENDS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            },
        )
        // RUN (active pill)
        NavigationBarItem(
            selected = currentRoute?.hasRoute<CorsaRoute.Home>() == true,
            onClick = { navController.navigate(CorsaRoute.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(Size.xm),
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
        // STATS
        NavigationBarItem(
            selected = currentRoute?.hasRoute<CorsaRoute.StatsScreen>() == true,
            onClick = { navController.navigate(CorsaRoute.StatsScreen) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    modifier = Modifier.size(Size.m),
                )
            },
            label = {
                Text(
                    "STATS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            },
        )
    }
}