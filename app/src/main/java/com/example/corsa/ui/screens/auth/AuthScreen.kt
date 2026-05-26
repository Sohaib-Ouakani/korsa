package com.example.corsa.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.AppBarText
import com.example.corsa.ui.composables.SmallSpacer
import com.example.corsa.ui.theme.Spacing

@Composable
fun AuthScreen(
    navController: NavController
) {
    Scaffold(
        topBar = { LoginTopBar() },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HeroText()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Button(
                    onClick = { navController.navigate(CorsaRoute.LoginScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.xxl),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = "Accedi",
                    )
                }
                OutlinedButton(
                    onClick = { navController.navigate(CorsaRoute.RegisterScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.xxl),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = "Registrati",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTopBar() {
    CenterAlignedTopAppBar(
        title = { AppBarText() },
    )
}

@Composable
fun HeroText() {
    Text(
        text = "READY TO\nMOVE?",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center,
    )
}