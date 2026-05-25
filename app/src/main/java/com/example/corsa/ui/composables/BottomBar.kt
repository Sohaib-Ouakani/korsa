package com.example.corsa.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute

@Composable
fun BottomBar(navController: NavController) {
    BottomAppBar() {
        // RUN (active pill)
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate(CorsaRoute.Home) },
            icon = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            label = {
                Text(
                    text = "RUN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )

        // FRIENDS
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(CorsaRoute.LoginTester) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            },
            label = {
                Text("FRIENDS", fontSize = 11.sp)
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )

        // STATS
        NavigationBarItem(
            selected = false,
            onClick = {  },
            icon = {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            },
            label = {
                Text("STATS", fontSize = 11.sp)
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )
    }
}