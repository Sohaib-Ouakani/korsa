package com.example.corsa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun StopWatchScreen(
    status: StopWatchStatus,
    navController: NavController
){
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .background(cs.primary)
            .fillMaxSize()
    ) {
        Text(
            text = status.formattedTime,
            color = cs.onPrimary,
            style = MaterialTheme.typography.displayMedium
        )
    }
}