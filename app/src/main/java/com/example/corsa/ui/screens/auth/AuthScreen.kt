package com.example.corsa.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute

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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Hero text pushed toward the center
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HeroText()
            }

            // Buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(CorsaRoute.LoginScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                    )
                }

                OutlinedButton(
                    onClick = { navController.navigate(CorsaRoute.RegisterScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
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
        title = {
            Text(
                text = "Korsa",
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 28.sp,
                letterSpacing = 2.sp,
            )
        },
    )
}

@Composable
fun HeroText() {
    Text(
        text = "READY TO\nMOVE?",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 56.sp,
        lineHeight = 58.sp,
        textAlign = TextAlign.Center,
    )
}



