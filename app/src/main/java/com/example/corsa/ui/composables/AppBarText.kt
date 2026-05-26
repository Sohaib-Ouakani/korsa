package com.example.corsa.ui.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppBarText() {
    Text(
        text = "Korsa",
        style = MaterialTheme.typography.titleMedium,
    )
}
