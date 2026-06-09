package com.example.corsa.ui.premissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

enum class LocationPermissionState {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

@Composable
fun LocationPermissionHandler(
    content: @Composable (state: LocationPermissionState, request: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    fun checkGranted() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PERMISSION_GRANTED

    var permissionState by remember {
        mutableStateOf(
            if (checkGranted()) LocationPermissionState.GRANTED
            else LocationPermissionState.DENIED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState = if (isGranted) {
            LocationPermissionState.GRANTED
        } else {
            LocationPermissionState.PERMANENTLY_DENIED
        }
    }

    content(
        permissionState
    ) { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
}
