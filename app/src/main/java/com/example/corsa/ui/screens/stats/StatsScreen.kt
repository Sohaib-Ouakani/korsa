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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.corsa.data.model.Run
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.ProfileStats
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.composables.UserEntry
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing


@Composable
fun StatsScreen(navController: NavController) {

    val cs = MaterialTheme.colorScheme
    val runentries = listOf<Run>(
//        Run("1", "J. Donahue", null, "2026-05-26T10:30:00+02:00", null, 5.4),
//        Run("2", "P. Aolo", null, "2026-05-26T10:30:00+02:00", null, 7.4)
    )
    val infoentries = UserEntry(
        "pippo",
        null,
        25f,
        1,
        7,
        100f
    )

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) },
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {

            // ── Header: nome + avatar ──────────────────────────────────────


            ProfileStats(navController, runentries, infoentries, { ProfileHeader(infoentries, cs) })
        }
    }
}

@Composable
private fun ProfileHeader(
    infoentries: UserEntry,
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
            text = infoentries.displayName,
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
                text = infoentries.displayName.first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}