package com.example.corsa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing

/**
 * STRIDE – Home Screen (pure View, no ViewModel wiring).
 *
 * Colour mapping used from MaterialTheme.colorScheme:
 *   primary          → lime accent (CTAs, active labels, pace text)
 *   onPrimary        → content on lime (black text/icons inside START button)
 *   background       → dark scaffold background
 *   onBackground     → primary text on background
 *   surface          → card background
 *   onSurface        → primary text on cards
 *   secondary        → subtle surface (icon button backgrounds, progress track)
 *   onSurfaceVariant → muted / secondary text and icons
 */
@Composable
fun HomeScreen(
    state: HomeState,
    navController: NavController
) {
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(Spacing.md))

            // ── Location label ───────────────────────────────────────────────
            // TODO: we can add new label to display other stats, like meteo
            LocationLabel(cs, state.locationName)

            Spacer(Modifier.height(Spacing.md))

            // ── Hero headline ────────────────────────────────────────────────
            // Uses Typography.displayLarge (ExtraBold Italic 60sp / lh 58sp)
            Text(
                text = "READY TO\nMOVE?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                color = cs.onSurface,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.xxl))

            // ── START button ─────────────────────────────────────────────────
            StartButton(cs) { navController.navigate(CorsaRoute.StopWatchScreen) }

            Spacer(Modifier.height(Spacing.xxl))

            // ── Goal card ────────────────────────────────────────────────────
            GoalCard(
                cs,
                state.goalKm,
                state.currentKm,
                state.progress
            )
            // TODO: we can add new cards to display other stats, like meteo
        }
    }
}

@Composable
private fun ColumnScope.StartButton(
    cs: ColorScheme,
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(Size.xxl)
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .background(cs.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStartClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start",
                tint = cs.onPrimary,
                modifier = Modifier.size(Size.l),
            )
            // Uses Typography.titleMedium (ExtraBold Italic 28sp)
            Text(
                text = "START",
                style = MaterialTheme.typography.titleMedium,
                color = cs.onPrimary,
            )
        }
    }
}

@Composable
private fun LocationLabel(cs: ColorScheme, locationName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(Size.s)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = locationName,
            color = cs.primary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun GoalCard(
    cs: ColorScheme,
    goalKm: Double,
    currentKm: Double,
    progress: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),  // was 16.dp → Spacing.md
        shape = MaterialTheme.shapes.medium,    // was RoundedCornerShape(16.dp) → CorsaShapes.medium (Spacing.md)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.cardSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "GOAL",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.2f KM".format(goalKm),
                        color = cs.onSurface,
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { progress.toFloat() },

                )
                Spacer(
                    modifier = Modifier.height(Spacing.sm)
                )
                Text(
                    text = "%.2f KM".format(currentKm),
                    color = cs.primary,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

//@Preview(
//    name = "STRIDE Home – Dark",
//    showBackground = true,
//    backgroundColor = 0xFF1A1A1A,
//    device = "spec:width=390dp,height=844dp,dpi=420",
//)
//@Composable
//fun StrideHomeScreenPreview() {
//    CorsaTheme(darkTheme = true, dynamicColor = false) {
//        HomeScreen()
//    }
//}