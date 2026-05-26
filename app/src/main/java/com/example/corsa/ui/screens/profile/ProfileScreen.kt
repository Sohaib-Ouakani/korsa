package com.example.corsa.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.example.corsa.ui.composables.AppBarText
import com.example.corsa.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {

    // TODO: load from supabase.auth.currentUserOrNull()
    val currentUsername = "mario_rossi"
    val currentEmail    = "mario@example.com"

    var newUsername by remember { mutableStateOf("") }
    var newEmail    by remember { mutableStateOf("") }

    // TODO: val isEmailUser = supabase.auth.currentUserOrNull()
    //     ?.identities?.any { it.provider == "email" } == true
    val isEmailUser = true

    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible     by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var showReauthDialog    by remember { mutableStateOf(false) }
    var currentPassword     by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSnackbar(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        topBar = { ProfileTopBar(onBack = { navController.popBackStack() }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Spacer(Modifier.height(Spacing.md))

            // ── Username ──────────────────────────────────────────────────
            SectionLabel("ACCOUNT")
            Spacer(Modifier.height(Spacing.xs))

            EditableField(
                currentValue = currentUsername,
                newValue = newUsername,
                onValueChange = { newUsername = it },
                label = "Username",
                keyboardType = KeyboardType.Text,
                onSave = {
                    // TODO: wrap in viewModelScope, call supabase, catch exception
                    // On success:
                    showSnackbar("Username aggiornato")
                    newUsername = ""
                    // On failure e.g.:
                    // showSnackbar("Errore: ${e.message}")
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))

            EditableField(
                currentValue = currentEmail,
                newValue = newEmail,
                onValueChange = { newEmail = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                onSave = {
                    // TODO: wrap in viewModelScope, call supabase, catch exception
                    // On success:
                    showSnackbar("Email aggiornata — controlla la tua casella per confermare")
                    newEmail = ""
                    // On failure e.g.:
                    // showSnackbar("Errore: ${e.message}")
                },
            )

            // ── Password (email users only) ───────────────────────────────
            if (isEmailUser) {
                Spacer(Modifier.height(Spacing.md))
                SectionLabel("SICUREZZA")
                Spacer(Modifier.height(Spacing.xs))

                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nuova password",
                    visible = newPasswordVisible,
                    onToggleVisibility = { newPasswordVisible = !newPasswordVisible },
                )

                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Conferma password",
                    visible = confirmPasswordVisible,
                    onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                )

                Spacer(Modifier.height(Spacing.xs))

                Button(
                    onClick = {
                        when {
                            newPassword.isBlank() -> showSnackbar("Inserisci una nuova password")
                            newPassword.length < 8 -> showSnackbar("La password deve essere di almeno 8 caratteri")
                            newPassword != confirmPassword -> showSnackbar("Le password non coincidono")
                            else -> showReauthDialog = true // validation passed, now ask for current password
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.xxl),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text("Cambia password")
                }

                if (showReauthDialog) {
                    ReauthDialog(
                        currentPassword = currentPassword,
                        currentPasswordVisible = currentPasswordVisible,
                        onPasswordChange = { currentPassword = it },
                        onToggleVisibility = { currentPasswordVisible = !currentPasswordVisible },
                        onDismiss = {
                            showReauthDialog = false
                            currentPassword = ""
                            currentPasswordVisible = false
                        },
                        onConfirm = {
                            showReauthDialog = false
                            scope.launch {
                                try {
                                    // TODO: replace with actual reauth + update call
                                    // supabase.auth.reauthenticate(password = currentPassword)
                                    // supabase.auth.updateUser { password = newPassword }

                                    // On success:
                                    newPassword = ""
                                    confirmPassword = ""
                                    currentPassword = ""
                                    currentPasswordVisible = false
                                    showSnackbar("Password aggiornata")
                                } catch (e: Exception) {
                                    showSnackbar("Errore: ${e.message ?: "password attuale errata"}")
                                }
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

// ── Editable field ────────────────────────────────────────────────────────────

@Composable
private fun EditableField(
    currentValue: String,
    newValue: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = "Attuale: $currentValue",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedTextField(
                value = newValue,
                onValueChange = onValueChange,
                label = { Text("Nuovo $label") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            )
            Button(
                onClick = onSave,
                enabled = newValue.isNotBlank(),
                shape = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(horizontal = Spacing.md),
                modifier = Modifier.height(Spacing.xxl),
            ) {
                Text("Salva")
            }
        }
    }
}

// ── Password field ────────────────────────────────────────────────────────────

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
) {
    val icon: ImageVector =
        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(imageVector = icon, contentDescription = null)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun ReauthDialog(
    currentPassword: String,
    currentPasswordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Conferma identità",
                style = MaterialTheme.typography.titleSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = "Inserisci la tua password attuale per continuare.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onPasswordChange,
                    label = { Text("Password attuale") },
                    singleLine = true,
                    visualTransformation =
                        if (currentPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (currentPasswordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = currentPassword.isNotBlank(),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
    )
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { AppBarText() },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro"
                )
            }
        }
    )
}