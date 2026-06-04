package com.example.corsa.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.corsa.ui.composables.BackTopBar
import com.example.corsa.ui.premissions.NotificationPermission
import com.example.corsa.ui.premissions.NotificationPermissionHandler
import com.example.corsa.ui.screens.splash.SplashScreen
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.AppError
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    state: SettingsState,
    actions: SettingsActions
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when(state.error) {
            is AppError.Present -> snackbarHostState.showSnackbar(state.error.message)
            else -> {}
        }
    }

    if (state.isLoading) {
        SplashScreen()
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { BackTopBar(navController) }
        ) { padding ->
            MainContent(
                state = state,
                actions = actions,
                padding = padding,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
private fun MainContent(
    state: SettingsState,
    actions: SettingsActions,
    padding: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: return@rememberLauncherForActivityResult
        actions.uploadAvatar(bytes, mimeType)
    }
    val scope = rememberCoroutineScope()

    val showSnackbar: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val ui = state.uiState
    val uiActions = actions.updateUIState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Spacer(Modifier.height(Spacing.md))

        SectionLabel("ACCOUNT")
        Spacer(Modifier.height(Spacing.xs))

        Avatar(photoPickerLauncher, state)

        Spacer(Modifier.height(Spacing.md))

        EditableField(
            currentValue = state.currentUsername,
            newValue = ui.newUsername,
            onValueChange = uiActions.onNewUsernameChange,
            label = "Username",
            keyboardType = KeyboardType.Text,
            onSave = {
                actions.saveNewUsername(ui.newUsername)
                uiActions.onNewUsernameChange("")
            },
        )

        if (state.isEmailUser) {
            Text(
                text = "Email: ${state.currentEmail}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.md))
            SectionLabel("SICUREZZA")
            Spacer(Modifier.height(Spacing.xs))

            PasswordField(
                value = ui.newPassword,
                onValueChange = uiActions.onNewPasswordChange,
                label = "Nuova password",
                visible = ui.newPasswordVisible,
                onToggleVisibility = { uiActions.onNewPasswordVisibleChange(!ui.newPasswordVisible) },
            )

            PasswordField(
                value = ui.confirmPassword,
                onValueChange = uiActions.onConfirmPasswordChange,
                label = "Conferma password",
                visible = ui.confirmPasswordVisible,
                onToggleVisibility = { uiActions.onConfirmPasswordVisibleChange(!ui.confirmPasswordVisible) },
            )

            Spacer(Modifier.height(Spacing.xs))

            Button(
                onClick = {
                    when {
                        ui.newPassword.isBlank() -> showSnackbar("Inserisci una nuova password")
                        ui.newPassword.length < 8 -> showSnackbar("La password deve essere di almeno 8 caratteri")
                        ui.newPassword != ui.confirmPassword -> showSnackbar("Le password non coincidono")
                        else -> uiActions.onShowReauthDialogChange(true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.xxl),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Cambia password")
            }

            Spacer(Modifier.height(Spacing.xs))

            if (ui.showReauthDialog) {
                ReauthDialog(
                    currentPassword = ui.currentPassword,
                    currentPasswordVisible = ui.currentPasswordVisible,
                    onPasswordChange = uiActions.onCurrentPasswordChange,
                    onToggleVisibility = { uiActions.onCurrentPasswordVisibleChange(!ui.currentPasswordVisible) },
                    onDismiss = {
                        uiActions.onShowReauthDialogChange(false)
                        uiActions.onCurrentPasswordChange("")
                        uiActions.onCurrentPasswordVisibleChange(false)
                    },
                    onConfirm = {
                        uiActions.onShowReauthDialogChange(false)
                        actions.saveNewPassword(ui.currentPassword, ui.newPassword)
                        uiActions.onNewPasswordChange("")
                        uiActions.onConfirmPasswordChange("")
                        uiActions.onCurrentPasswordChange("")
                        uiActions.onCurrentPasswordVisibleChange(false)
                    },
                )
            }
        }

        Spacer(Modifier.height(Spacing.xl))
        SectionLabel("NOTIFICHE")
        Spacer(Modifier.height(Spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Sfida settimanale",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Ricevi un promemoria ogni settimana",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            NotificationPermissionHandler { notifState, requestNotification ->
                var pendingEnable by remember { mutableStateOf(false) }

                LaunchedEffect(notifState) {
                    if (pendingEnable && notifState == NotificationPermission.GRANTED) {
                        actions.toggleWeeklyNotification()
                        pendingEnable = false
                    }
                }

                Switch(
                    checked = state.weeklyNotificationEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            when (notifState) {
                                NotificationPermission.GRANTED -> actions.toggleWeeklyNotification()
                                else -> {
                                    pendingEnable = true
                                    requestNotification()
                                }
                            }
                        } else {
                            actions.toggleWeeklyNotification()
                        }
                    },
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

        OutlinedButton(
            onClick = actions.logout,
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.xxl),
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

@Composable
private fun ColumnScope.Avatar(
    photoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    state: SettingsState
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .clickable {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (state.avatarUrl != null) {
            AsyncImage(
                model = state.avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Tap-to-edit overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(28.dp),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambia foto",
                tint = Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .size(16.dp)
                    .wrapContentSize(Alignment.Center),
            )
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