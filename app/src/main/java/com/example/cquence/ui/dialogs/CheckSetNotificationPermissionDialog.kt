package com.example.cquence.ui.dialogs

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermission() {
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
            ),
            title = {
                Text(
                    text = "Notification Permission",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "You need to allow this permission in order for the app to run properly",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { permissionState.launchPermissionRequest() },
                ) {
                    Text("Confirm")
                }
            },
            onDismissRequest = {},
        )
    } else {
        LaunchedEffect(key1 = Unit) {
            this.launch {
                permissionState.launchPermissionRequest()
            }
        }
    }
}