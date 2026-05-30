package com.example.corsa.ui.screens.settings

import android.util.Log
import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.corsa.ui.composables.AppBarText
import com.example.corsa.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsInfo: SettingsInfo?,
    state: SettingsState,
    onLogOut: () -> Unit,
    onSaveNewUsername: (String) -> Unit,
    onSaveNewPassword: (oldPassword: String, newPassword: String) -> Unit,
    onClearError: () -> Unit,
) {
    var newUsername by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var showReauthDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showSnackbar: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(state) {
        if (state is SettingsState.Error) {
            snackbarHostState.showSnackbar(state.message)
        }
    }

    Scaffold(
        topBar = { ProfileTopBar(onBack = { navController.popBackStack() }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->

        // Show a full-screen loader while profile hasn't arrived yet
        if (settingsInfo == null || state is SettingsState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Spacer(Modifier.height(Spacing.md))

            SectionLabel("ACCOUNT")
            Spacer(Modifier.height(Spacing.xs))

            EditableField(
                currentValue = settingsInfo.currentUsername,
                newValue     = newUsername,
                onValueChange = { newUsername = it },
                label        = "Username",
                keyboardType = KeyboardType.Text,
                onSave       = {
                    onSaveNewUsername(newUsername)
                    newUsername = ""
                },
            )

            if (settingsInfo.isEmailUser) {
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
                        // Password safety check
                        when {
                            newPassword.isBlank() -> showSnackbar("Inserisci una nuova password")
                            newPassword.length < 8 -> showSnackbar("La password deve essere di almeno 8 caratteri")
                            newPassword != confirmPassword -> showSnackbar("Le password non coincidono")
                            else -> showReauthDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(Spacing.xxl),
                    shape    = MaterialTheme.shapes.large,
                ) {
                    Text("Cambia password")
                }

                Spacer(Modifier.height(Spacing.xs))

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
                            onSaveNewPassword(currentPassword, newPassword)
                            // Optimistically clear fields; errors surface via snackbar
                            newPassword = ""
                            confirmPassword = ""
                            currentPassword = ""
                            currentPasswordVisible = false
                        },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

            OutlinedButton(
                onClick = onLogOut,
                modifier = Modifier.fillMaxWidth().height(Spacing.xxl),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            ) {
                Text("Logout")
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

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