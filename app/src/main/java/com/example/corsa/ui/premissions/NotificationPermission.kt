package com.example.corsa.ui.premissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

enum class NotificationPermission {
    GRANTED,
    DENIED,
}

@Composable
fun NotificationPermissionHandler(
    content: @Composable (state: NotificationPermission, request: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    fun checkNotificationGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PERMISSION_GRANTED
    } else true

    var permissionState by remember {
        mutableStateOf(
            if (checkNotificationGranted()) NotificationPermission.GRANTED
            else NotificationPermission.DENIED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState = if (isGranted) {
            NotificationPermission.GRANTED
        } else {
            NotificationPermission.DENIED
        }
    }

    // Below Android 13 notifications don't need runtime permission,
    val request: () -> Unit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    } else {
        {}
    }

    content(permissionState, request)
}