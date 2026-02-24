package com.fabricio.posedetector.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Wrapper function to ensure the app has camera permissions
@Composable
fun PermissionGate(
    content: @Composable () -> Unit
) {
    // Get the current context
    val context = LocalContext.current
    // Gets the permission string from the our manifest
    val permission = Manifest.permission.CAMERA

    // 'remember' keeps the state across recompositions
    // 'mutableStateOf' makes the value observable and triggers recomposition when changed
    // 'by' delegates the MutableState so we can use hasPermission directly instead of .value
    // The initial value checks whether the camera permission is already granted
    // So hasPermission is observable state
    // When its value changes, Compose recomposes any UI that reads it
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Creates a lifecycle-aware launcher that can request a permission
    // When launcher.launch(permission) is called, the system shows the permission dialog
    // The lambda receives the result (true if granted, false if denied)
    // If granted, we update hasPermission which triggers recomposition

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
            hasPermission = granted
    }

    // Runs this side-effect when the composable first enters the composition
    // Since the key is Unit (which never changes), this runs only once
    // If permission is not granted, we trigger the system permission dialog
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(permission)
        }
    }

    // If permission is granted show the cameraScreen else an error message
    if (hasPermission) {
        content()
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required to run this app :(")
        }
    }
}