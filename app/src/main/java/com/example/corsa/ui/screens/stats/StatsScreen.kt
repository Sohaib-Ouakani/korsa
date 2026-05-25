package com.example.corsa.ui.screens.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar

@Composable
fun StatsScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Classifica") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Feed") }
                )

            }

            when (selectedTab) {
                0 -> ClassificaContent()
                1 -> FeedContent()
            }
        }
    }
}

@Composable
fun FeedContent() {

}

@Composable
fun ClassificaContent() {

}