package com.example.corsa.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.corsa.ui.CorsaRoute
import androidx.navigation.NavDestination.Companion.hasRoute

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination

    BottomAppBar() {
        // RUN (active pill)
        NavigationBarItem(
            selected = currentRoute?.hasRoute<CorsaRoute.Home>() == true,
            onClick = { navController.navigate(CorsaRoute.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
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

        // FRIENDS
        NavigationBarItem(
            selected = currentRoute?.hasRoute<CorsaRoute.LoginTester>() == true,
            onClick = { navController.navigate(CorsaRoute.LoginTester) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
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

        // STATS
        NavigationBarItem(
            selected = currentRoute?.hasRoute<CorsaRoute.StatsScreen>() == true,
            onClick = { navController.navigate(CorsaRoute.StatsScreen) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
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