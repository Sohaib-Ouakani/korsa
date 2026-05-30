package com.example.corsa.ui.premissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

/**
 * Holds the current state of the location permission.
 */
enum class LocationPermissionState {
    GRANTED,       // good to go
    DENIED,        // user said no (can ask again)
    PERMANENTLY_DENIED  // user ticked "don't ask again"
}

/**
 * A composable that manages the location permission lifecycle.
 *
 * Usage:
 *   LocationPermissionHandler { state, request ->
 *       when (state) {
 *           GRANTED            -> ShowMap()
 *           DENIED             -> AskButton(onClick = request)
 *           PERMANENTLY_DENIED -> ShowSettingsPrompt()
 *       }
 *   }
 */
@Composable
fun LocationPermissionHandler(
    content: @Composable (state: LocationPermissionState, request: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    // Check the current permission status synchronously
    fun checkGranted() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PERMISSION_GRANTED

    var permissionState by remember {
        mutableStateOf(
            if (checkGranted()) LocationPermissionState.GRANTED
            else LocationPermissionState.DENIED
        )
    }

    // This launcher shows the system permission dialog
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