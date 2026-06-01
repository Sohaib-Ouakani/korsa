package com.example.corsa.ui.screens.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.BackTopBar
import com.example.corsa.ui.composables.BottomBar

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFollowScreen(
    navController: NavController,
    searchState: SearchState,
    action: FollowAction
) {
    var query by remember { mutableStateOf("") }

    // Replace with real ViewModel state


     if (query.isBlank()) {
        searchState.notFriends
    } else {
        searchState.notFriends.filter { it.username.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = { BackTopBar(navController) },
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            // ── Search bar ────────────────────────────────────────────────
            SearchBar(searchState, action, navController)

            Spacer(modifier = Modifier.height(20.dp))

        }
    }
}

// ── Search bar component ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(searchState: SearchState,
              action: FollowAction, navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val suggested = searchState.notFriends
    val filteredSuggested = if (query.isBlank()) {
        suggested
    } else {
        suggested.filter { it.username.contains(query, ignoreCase = true) }
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
        onExpandedChange = {},
        shape = searchBarShape,
        windowInsets = WindowInsets(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(searchBarShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { traversalIndex = 1f }
        ) {
            if (filteredSuggested.isNotEmpty()) {
                filteredSuggested.forEach { friend ->
                    ListItem(
                        headlineContent = { Text(friend.username) },
                        leadingContent = {
                            // Avatar circle
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
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.clickable {
                            query = friend.username
                            navController.navigate(CorsaRoute.ProfileDetailScreen(friend.id))
                        }
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
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable {
                    }
                )
            }
        }
    }
}




