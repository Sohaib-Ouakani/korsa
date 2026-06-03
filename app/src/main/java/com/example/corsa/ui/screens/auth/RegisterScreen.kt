package com.example.corsa.ui.screens.auth

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
import com.example.corsa.ui.composables.AppBarText
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.AppError
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    navController: NavController,
    state: RegisterState,
    actions: RegisterActions
) {
    val composeAuth = koinInject<ComposeAuth>()
    val googleAuthState = composeAuth.rememberSignInWithGoogle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        when (state.error) {
            is AppError.Present -> snackbarHostState.showSnackbar(state.error.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = { RegisterScreenTopBar(onBack = {
            navController.popBackStack()
            actions.clearError()
        }) },
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
                EmailField(email = state.email, onEmailChange = actions.onEmailChange)
                PasswordField(
                    password = state.password,
                    onPasswordChange = actions.onPasswordChange,
                    passwordVisible = state.passwordVisible,
                    onToggleVisibility = actions.onTogglePasswordVisibility
                )
                RegisterDivider()
                GoogleButton(
                    onClick = { googleAuthState.startFlow() },
                    enabled = !state.isLoading
                )
                RegisterButton(
                    onClick = { actions.registerWithEmail() },
                    isLoading = state.isLoading
                )
            }
        }
    }
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
            text = "LET'S\nGO!",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenTopBar(onBack: () -> Unit) {
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
private fun EmailField(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
) {
    val visibilityIcon: ImageVector =
        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
    val visibilityDesc = if (passwordVisible) "Nascondi password" else "Mostra password"

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        singleLine = true,
        visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(imageVector = visibilityIcon, contentDescription = visibilityDesc)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun RegisterButton(onClick: () -> Unit, isLoading: Boolean) {
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
                strokeWidth = 2.dp
            )
        } else {
            Text(text = "Registrati")
        }
    }
}

@Composable
private fun GoogleButton(onClick: () -> Unit, enabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(Spacing.xxl),
        shape = MaterialTheme.shapes.large,
    ) {
        Text(text = "Continua con Google")
    }
}

@Composable
private fun RegisterDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "altrimenti",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}