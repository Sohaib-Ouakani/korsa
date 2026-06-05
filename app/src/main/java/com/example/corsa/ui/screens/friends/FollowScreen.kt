package com.example.corsa.ui.screens.friends

import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.AppError
import com.example.corsa.utils.toFeedDateString

enum class StatsTab(val label: String) {
    Rank("Classifica"),
    Feed("Feed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    navController: NavController,
    followState: FollowState,
    searchState: SearchState,
    action: FollowAction
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(followState) {
        when(followState.error) {
            is AppError.Present -> snackbarHostState.showSnackbar(followState.error.message)
            else -> {}
        }
    }
    var selectedTab by remember { mutableStateOf(StatsTab.Rank) }
    val tabs = StatsTab.entries
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            action.refreshFriends()
        }
    }
    Scaffold(
        topBar = { TopBar(navController, searchState.myProfileUrl) },
        bottomBar = { BottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { FloatingActionButton(
            onClick = { navController.navigate(CorsaRoute.AddFollowScreen) },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.PersonAdd, "Add Friends")
        } }
    ) { contentPadding ->
        Column(
            modifier = Modifier
            .padding(contentPadding)
        ) {
            Spacer(Modifier.height(16.dp))

            FriendSearchBar(searchState, navController, action)

            // ── Primary Row  ────────────────────────────────────────────────
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
                StatsTab.Rank -> Rank(followState, navController, action)
                StatsTab.Feed -> Feed(followState, navController, action)
            }
        }
    }
}

// ── Feed part  ────────────────────────────────────────────────

@Composable
fun Feed(state: FollowState, navController: NavController, action: FollowAction) {
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
    FeedList(state, navController, action)
        }
}


@Composable
fun FeedList(state: FollowState, navController: NavController, action: FollowAction) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(Spacing.sm, Spacing.sm, Spacing.sm, 80.dp),
    ) {
        items(state.feedEntry) { entry ->
            FeedCard(entry = entry, navController, action)
        }
    }
}


@Composable
fun FeedCard(entry: RunFeedEntry, navController: NavController, action: FollowAction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {navController.navigate(CorsaRoute.RunDetailScreen(entry.runId))}
    ) {

        // ── Immagine percorso (elemento principale) ───────────────────
        if (entry.pathUrl != null) {
            AsyncImage(
                model              = entry.pathUrl,
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
            val avatarUrl = action.getAvatarUrl(entry.userId)
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model              = avatarUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text  = entry.displayName.first().uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = entry.displayName,
                    style      = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text  = entry.startTime.toFeedDateString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "%.2f".format(entry.distance),
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

// ── Searchbar part  ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSearchBar(searchState: SearchState, navController: NavController, action: FollowAction) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val allFriends = searchState.friendsName
    val filteredFriends = if (query.isBlank()) {
        emptyList()
    } else {
        allFriends.filter { it.username.contains(query, ignoreCase = true) }
    }

    LaunchedEffect(query) {
        if (query.isBlank()) expanded = false
    }

    val searchBarShape = RoundedCornerShape(28.dp)

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Search Friends...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (expanded) {
                        IconButton(onClick = {
                            query = ""
                            expanded = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        shape = searchBarShape,
        windowInsets = WindowInsets(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .clip(searchBarShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { traversalIndex = 1f }
        ) {
            if (filteredFriends.isNotEmpty()) {
                filteredFriends.forEach { friend ->
                    ListItem(
                        headlineContent = { Text(friend.username) },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.clickable {
                            query = friend.username
                            navController.navigate(CorsaRoute.ProfileDetailScreen(friend.id))
                            expanded = false
                        },
                    )
                }
            } else if (query.isNotBlank()) {
                ListItem(
                    headlineContent = {
                        Text(
                            "No friends found",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarUrl = friend.avatarPath?.let {
                                action.buildAvatarUrl(it)
                            }
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "${friend.username}'s avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.clickable {
                        expanded = false
                    }
                )
            }
        }
    }
}
// ── Rank part  ────────────────────────────────────────────────

enum class RankTab(val label: String) {
    Kilometers("Kilometri"),
    Level("Livello")
}

@Composable
fun Rank(followState: FollowState, navController: NavController, action: FollowAction) {
    var rankSelectedTab by remember { mutableStateOf(RankTab.Kilometers) }
    val tabs = RankTab.entries


    LaunchedEffect(rankSelectedTab) {
         action.loadRanking(
            when (rankSelectedTab) {
                RankTab.Kilometers -> SortBy.Kilometers
                RankTab.Level      -> SortBy.Level
            }
        )
    }

    Column {
        SecondaryTabRow(selectedTabIndex = tabs.indexOf(rankSelectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = rankSelectedTab == tab,
                    onClick  = { rankSelectedTab = tab },
                    text     = { Text(tab.label) }
                )
            }
        }

        if (followState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),  // ← guaranteed space
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            RankList(
                entries = followState.rankEntry,
                sortBy  = when (rankSelectedTab) {
                    RankTab.Kilometers -> SortBy.Kilometers
                    RankTab.Level      -> SortBy.Level
                },
                navController,
                action
            )
        }
    }
}

@Composable
fun RankList(entries: List<UserRankEntry>, sortBy: SortBy, navController: NavController, action: FollowAction) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(Spacing.sm, Spacing.sm, Spacing.sm, 80.dp),
    ) {
        itemsIndexed(entries) { index, entry ->
            RankCard(
                position = index + 1,
                entry    = entry,
                sortBy   = sortBy,
                navController,
                action
            )
        }
    }
}

@Composable
fun RankCard(position: Int, entry: UserRankEntry, sortBy: SortBy, navController: NavController, action: FollowAction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {navController.navigate(CorsaRoute.ProfileDetailScreen(entry.userId))}
    ) {
        Row(
            modifier              = Modifier.padding(Spacing.md, Spacing.md),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.width(28.dp),
            ) {
                if (position == 1) {
                    Icon(
                        imageVector        = Icons.Outlined.EmojiEvents, // trofeo
                        contentDescription = "1st place",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                Text(
                    text       = "$position",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
            }
            // Sostituisci Box con AsyncImage (Coil) quando hai le foto reali
            var avatarUrl = action.getAvatarUrl(entry.userId)
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model              = avatarUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text  = entry.displayName.first().uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    )
            }
            val (statLabel, statValue) = when (sortBy) {
                SortBy.Kilometers -> "KM"  to "%.1f".format(entry.weekKm)
                SortBy.Level      -> "LVL" to entry.level.toString()
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = statValue,
                    style      = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text  = statLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

