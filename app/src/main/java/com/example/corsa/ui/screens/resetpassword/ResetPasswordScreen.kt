package com.example.corsa.ui.screens.resetpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.AppBarText
import com.example.corsa.ui.composables.BackTopBar
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.AppError

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    state: ResetPasswordState,
    actions: ResetPasswordActions
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        when (state.error) {
            is AppError.Present -> snackbarHostState.showSnackbar(state.error.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = { BackTopBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HeroText()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                ResetPasswordField(
                    password = state.password,
                    onPasswordChange = actions.onPasswordChange,
                    passwordVisible = state.passwordVisible,
                    onTogglePasswordVisibility = actions.onTogglePasswordVisibility,
                )
                ResetPasswordButton(
                    onClick = { actions.resetPassword { navController.navigate(CorsaRoute.Home) } },
                    isLoading = state.isLoading,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResetPasswordTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { AppBarText() },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
private fun ColumnScope.HeroText() {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RESET\nPASSWORD",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResetPasswordField(
    password: String,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    val visibilityIcon: ImageVector =
        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
    val visibilityDesc =
        if (passwordVisible) "Hide password" else "Show password"

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Nuova password") },
        singleLine = true,
        visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(imageVector = visibilityIcon, contentDescription = visibilityDesc)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun ResetPasswordButton(
    onClick: () -> Unit,
    isLoading: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(Spacing.xxl),
        shape = MaterialTheme.shapes.large,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Spacing.md),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = "Reimposta password")
        }
    }
}