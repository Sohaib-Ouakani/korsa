package com.example.corsa.ui.screens.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar

enum class StatsTab(val label: String) {
    Rank("Classifica"),
    Feed("Feed")
}

@Composable
fun StatsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(StatsTab.Rank) }
    val tabs = StatsTab.entries

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            PrimaryTabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }

            when (selectedTab) {
                StatsTab.Rank -> Rank()
                StatsTab.Feed -> Feed()
            }
        }
    }
}

@Composable
fun Feed() {

}

enum class RankTab(val label: String) {
    Kilometers("Kilometri"),
    Level("Livello")
}

@Composable
fun Rank() {
    var rankSelectedTab by remember { mutableStateOf(RankTab.Kilometers) }
    val tabs = RankTab.entries

    Column {
        SecondaryTabRow(selectedTabIndex = tabs.indexOf(rankSelectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = rankSelectedTab == tab,
                    onClick = { rankSelectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        when (rankSelectedTab) {
            RankTab.Kilometers -> { }
            RankTab.Level -> { }
        }
    }
}