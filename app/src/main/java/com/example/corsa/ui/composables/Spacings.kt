package com.example.corsa.ui.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.corsa.ui.theme.Spacing

@Composable
fun SmallSpacer() {
    Spacer(Modifier.height(Spacing.sm))
}

@Composable
fun MediumSpacer() {
    Spacer(Modifier.height(Spacing.md))
}
