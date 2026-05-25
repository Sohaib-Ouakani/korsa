package com.example.corsa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.theme.CorsaTheme

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
fun StrideHomeScreen(
    // --- display data ---
    locationName: String = "CENTRAL PARK",
    lastRunKm: Double = 5.42,
    lastRunPaceMin: Int = 4,
    lastRunPaceSec: Int = 45,
    goalKm: Double = 25.0,
    goalProgressFraction: Float = 0.65f,
    // --- callbacks ---
    onStartClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onGoalEditClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = cs.background,
        bottomBar = {
            BottomBar(
                onRunClick     = onStartClick,
                onFriendsClick = onFriendsClick,
                onStatsClick   = onStatsClick,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Top bar ──────────────────────────────────────────────────────
            TopBar(
                onMenuClick    = onMenuClick,
                onProfileClick = onProfileClick,
            )

            Spacer(Modifier.height(16.dp))

            // ── Location label ───────────────────────────────────────────────
            LocationLable(cs, locationName)

            Spacer(Modifier.height(16.dp))

            // ── Hero headline ────────────────────────────────────────────────
            Text(
                text = "READY TO\nMOVE?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = cs.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 56.sp,
                lineHeight = 58.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            // ── START button ─────────────────────────────────────────────────
            StartButton(cs, onStartClick)

            Spacer(Modifier.height(36.dp))

            // ── Last Run card ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "LAST RUN",
                            color = cs.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.2f".format(lastRunKm),
                                color = cs.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp,
                                modifier = Modifier.alignByBaseline(),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "KM",
                                color = cs.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.alignByBaseline(),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "%02d:%02d /KM".format(lastRunPaceMin, lastRunPaceSec),
                            color = cs.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }

                    // History icon button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cs.secondary)
                            .clickable(onClick = onHistoryClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("↺", color = cs.onSurfaceVariant, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Goal card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GOAL",
                            color = cs.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.1f".format(goalKm),
                                color = cs.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp,
                                modifier = Modifier.alignByBaseline(),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "KM",
                                color = cs.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.alignByBaseline(),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        // Progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(cs.secondary),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(goalProgressFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(cs.primary),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${(goalProgressFraction * 100).toInt()}% OF WEEKLY TARGET",
                            color = cs.onSurfaceVariant,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Edit goal icon button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cs.secondary)
                            .clickable(onClick = onGoalEditClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("⟳", color = cs.onSurfaceVariant, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
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
            .size(220.dp)
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
                modifier = Modifier.size(52.dp),
            )
            Text(
                text = "START",
                color = cs.onPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                letterSpacing = 3.sp,
            )
        }
    }
}

@Composable
private fun LocationLable(cs: ColorScheme, locationName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(cs.primary)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = locationName,
            color = cs.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
        )
    }
}

// ── Reusable dark card ────────────────────────────────────────────────────────
@Composable
private fun Card(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { Column(modifier = Modifier.padding(20.dp), content = content) }
    )
}

@Preview(
    name = "STRIDE Home – Dark",
    showBackground = true,
    backgroundColor = 0xFF1A1A1A,
    device = "spec:width=390dp,height=844dp,dpi=420",
)
@Composable
fun StrideHomeScreenPreview() {
    CorsaTheme(darkTheme = true, dynamicColor = false) {
        StrideHomeScreen()
    }
}


