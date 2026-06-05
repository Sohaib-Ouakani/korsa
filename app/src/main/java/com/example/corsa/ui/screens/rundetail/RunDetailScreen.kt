package com.example.corsa.ui.screens.rundetail

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.corsa.ui.composables.BackTopBar
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.formatDistance
import com.example.corsa.utils.formatDuration
import com.example.corsa.utils.formatPace
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import androidx.core.graphics.toColorInt
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.screens.splash.SplashScreen
import com.example.corsa.utils.AppError
import com.example.corsa.utils.latLngs
import com.example.corsa.utils.parseRunGeoJson
import com.example.corsa.utils.toFeedDateString

private const val ROUTE_SOURCE_ID = "run-route-source"
private const val ROUTE_LAYER_ID  = "run-route-layer"
private const val MAP_STYLE_URL   = "https://tiles.openfreemap.org/styles/liberty"
private const val ROUTE_COLOR     = "#FF4500"   // orange-red
private const val ROUTE_WIDTH     = 5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunDetailScreen(
    navController: NavController,
    state: RunDetailState,
    actions: RunDetailActions
) {
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when(state.error) {
            is AppError.Present -> snackbarHostState.showSnackbar(state.error.message)
            else -> {}
        }
    }

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    if (state.isLoading) {
        SplashScreen()
    } else {
        BottomSheetScaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            scaffoldState = sheetState,
            topBar = {
                BackTopBarWithShareRun(navController, state, context)
            },
            sheetPeekHeight = 260.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetContent = {
                RunDetailSheetContent(
                    run = state.run,
                    comments = state.comments,
                    likeCount = state.likeCount,
                    runnerProfile = state.runnerProfile,
                    onProfileNavigation = { userId ->
                        if (userId == state.myUserId) {
                            navController.navigate(CorsaRoute.StatsScreen)
                        } else {
                            navController.navigate(CorsaRoute.ProfileDetailScreen(userId))
                        }
                    },
                    toggleLike = actions.toggleLike,
                    alreadyLiked = state.alreadyLiked,
                    onAddComment = actions.onAddComment,
                    spacing = Spacing.md,
                )
            }
        ) { paddingValues ->
            RunDetailMap(
                geoJson = state.run.path.toString(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun BackTopBarWithShareRun(
    navController: NavController,
    state: RunDetailState,
    context: Context
) {
    BackTopBar(
        navController = navController,
        actions = {
            IconButton(onClick = {
                val token = state.run.shareToken ?: return@IconButton
                val shareUrl = "corsa://run/$token"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        shareUrl
                    )
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share run via")
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share run"
                )
            }
        }
    )
}

@Composable
fun RunDetailMap(
    geoJson: String,
    modifier: Modifier = Modifier
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    MapLibre.getInstance(context)

    // Hoist MapView so lifecycle observer and AndroidView share the same instance
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner)  { mapView.onStart() }
            override fun onResume(owner: LifecycleOwner) { mapView.onResume() }
            override fun onPause(owner: LifecycleOwner)  { mapView.onPause() }
            override fun onStop(owner: LifecycleOwner)   { mapView.onStop() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    val overlayColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        // ── factory: runs ONCE — set up style, source, layer, camera here ──
        factory = { _ ->
            mapView.also { mv ->
                mv.getMapAsync { map ->
                    map.setStyle(MAP_STYLE_URL) { style ->
                        val fc = parseRunGeoJson(geoJson)
                        val latLngs = fc.latLngs()

                        Log.d("RunDetailMap", "Feature count: ${fc.features()?.size}")
                        Log.d("RunDetailMap", "LatLng count: ${latLngs.size}")

                        // 1. Source
                        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID, fc))

                        // 2. Layer (source must already be added above)
                        style.addLayer(
                            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
                                .withProperties(
                                    PropertyFactory.lineColor(overlayColor),
                                    PropertyFactory.lineWidth(ROUTE_WIDTH),
                                    PropertyFactory.lineCap("round"),
                                    PropertyFactory.lineJoin("round")
                                )
                        )

                        // 3. Camera

                        when {
                            latLngs.size >= 2 -> {
                                val bounds = LatLngBounds.Builder()
                                    .includes(latLngs)
                                    .build()
                                val paddingPx = (80 * context.resources.displayMetrics.density).toInt()
                                map.easeCamera(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        paddingPx, paddingPx, paddingPx, paddingPx
                                    ),
                                    600
                                )
                            }
                            latLngs.size == 1 -> {
                                map.easeCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14.0),
                                    600
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier,
        // ── update: runs on recomposition — only refresh source data ──────
        update = { mv ->
            mv.getMapAsync { map ->
                val style = map.style ?: return@getMapAsync
                (style.getSource(ROUTE_SOURCE_ID) as? GeoJsonSource)
                    ?.setGeoJson(parseRunGeoJson(geoJson))
            }
        }
    )
}

@Composable
fun RunDetailSheetContent(
    run: Run,
    runnerProfile: Profile,
    likeCount: Int,
    comments: List<CommentEntry>,
    spacing: Dp,
    onProfileNavigation: (userId: String) -> Unit,
    toggleLike: () -> Unit,
    onAddComment: (String) -> Unit,
    alreadyLiked: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = spacing,
            end = spacing,
            top = Spacing.sm,
            bottom = Spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {

        item {
            UserDateRow(
                runnerProfile = runnerProfile,
                startTime = run.startTimeLocal.toFeedDateString(),
                onProfileNavigation = onProfileNavigation
            )
        }

        item {
            StatCardsGrid(run = run)
        }

        item {
            LikesCommentsRow(
                likeCount = likeCount,
                commentCount = comments.size,
                toggleLike = toggleLike,
                alreadyLiked = alreadyLiked
            )
        }

        item {
            AddCommentRow(
                onAddComment = onAddComment,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }

        items(comments, key = { it.commentId }) { comment ->
            CommentItem(comment, onProfileNavigation)
        }
    }
}

@Composable
fun UserDateRow(
    runnerProfile: Profile,
    startTime: String,
    onProfileNavigation: (String) -> Unit
) {
    val dateLabel = startTime.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = runnerProfile.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = { onProfileNavigation(runnerProfile.id) } ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatCardsGrid(run: Run) {
    // Build the list dynamically so optional stats appear only when present
    val stats = buildList {
        add("Distance"  to formatDistance(run.distanceMeters))
        add("Pace"       to formatPace(run.meanPaceSeconds))
        add("Duration"   to formatDuration(run.startTime, run.endTime))
        run.temperature?.let  { add("Temp"      to "%.1f °C".format(it)) }
        run.elevationGain?.let { add("Elevation" to "+%.0f m".format(it)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        stats.chunked(3).forEach { rowStats ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowStats.forEach { (label, value) ->
                    StatCard(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining slots in the last row so cards stay same width
                repeat(3 - rowStats.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
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
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.sm, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun LikesCommentsRow(
    likeCount: Int,
    commentCount: Int,
    toggleLike: () -> Unit,
    alreadyLiked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$commentCount comments",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            IconButton(
                onClick = toggleLike,
            ) {
                Icon(
                    imageVector = if (alreadyLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Likes",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "$likeCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddCommentRow(
    onAddComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = "Add a comment…",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall
        )

        IconButton(
            onClick = {
                val trimmed = text.trim()
                if (trimmed.isNotEmpty()) {
                    onAddComment(trimmed)
                    text = ""
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Post comment",
                tint = if (text.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentEntry,
    onProfileNavigation: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.xl)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { onProfileNavigation(comment.authorId) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = comment.authorUsername.first().uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.authorUsername,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = comment.commentCreatedAt.toFeedDateString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = comment.commentContent,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RunDetailError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}