package com.example.corsa.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.corsa.data.model.Run
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.screens.friends.formatFeedDate
import com.example.corsa.ui.theme.Spacing


data class UserEntry(
    val displayName: String,
    val avatarUrl: String?,
    val weekKm: Float,
    val level: Int,
    val completedChallenge: Int,
    val totKm: Float
)

@Composable
fun ProfileStats(
    navController: NavController,
    runentries: List<Run>,
    infoentries: UserEntry,
    header: @Composable () -> Unit = {},
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(Spacing.sm, Spacing.sm, Spacing.sm, 80.dp),
    ) {
        item {
            header()
        }
        // ── Griglia statica come singolo item ──────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    StatCard("LIVELLO", infoentries.level.toString(), modifier = Modifier.weight(1f))
                    StatCard("Challenge\nCompletate", infoentries.completedChallenge.toString(), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    StatCard("Totale Km", infoentries.totKm.toString(), modifier = Modifier.weight(1f))
                    StatCard("Km Settimanali", infoentries.weekKm.toString(), modifier = Modifier.weight(1f))
                }
            }
        }
        item { Spacer(Modifier.height(Spacing.md)) }
        item {
            Text(
                text = "Corse Passate:",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                style = MaterialTheme.typography.displayMedium,
            )
        }

        // ── Run cards ─────────────────────────────────────────────────
        items(runentries) { entry ->
            RunCard(entry = entry, navController)
        }
    }
}

@Composable
fun RunCard(entry: Run, navController: NavController) {
    Card(modifier = Modifier.fillMaxWidth(),
        onClick = { navController.navigate(CorsaRoute.RunDetailScreen(entry.id)) }
    ) {

        // ── Immagine percorso (elemento principale) ───────────────────
        if (entry.previewPath != null) {
            AsyncImage(
                model              = entry.previewPath,
                contentDescription = "Percorso corsa",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        } else {
            // Placeholder se non c'è immagine
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Map,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(48.dp),
                )
            }
        }

        // ── Informazioni sotto l'immagine ─────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Avatar
            Column(modifier = Modifier.weight(1f)) {
                //nel caso si puo mettere la citta
//                Text(
//                    text       = entry.displayName,
//                    style      = MaterialTheme.typography.labelLarge,
//                )
                Text(
                    text  = formatFeedDate(entry.startTime.toString()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "%.2f".format((entry.distanceMeters)/1000),
                    style      = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text  = "KM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Occupa tutto lo spazio della Card
                .padding(horizontal = Spacing.sm, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centra verticalmente
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )



        }
    }
}