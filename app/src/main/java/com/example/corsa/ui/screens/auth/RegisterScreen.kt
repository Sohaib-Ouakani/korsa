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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    navController: NavController,
    state: AuthState,
    authActions: AuthActions
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val supabase = koinInject<SupabaseClient>() // This is needed here for the composable

    val googleAuthState = supabase.composeAuth.rememberSignInWithGoogle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when(state.error) {
            is AppError.Present -> snackbarHostState.showSnackbar(state.error.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = { RegisterScreenTopBar(onBack = { navController.popBackStack() }) },
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
                EmailField(email = email, onEmailChange = { email = it })
                PasswordField(
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )
                RegisterDivider()
                GoogleButton(
                    onClick = { googleAuthState.startFlow() },
                    enabled = !state.isLoading
                )
                RegisterButton(
                    onClick = { authActions.registerWithEmail(email, password) },
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