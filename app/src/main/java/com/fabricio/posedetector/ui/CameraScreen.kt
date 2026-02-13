package com.fabricio.posedetector.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

// Creates a traditional android camera preview and binds it to the app lifecycle so it turns on and off automatically
@Composable
fun CameraScreen(){
    // Gets the LifeCycleOwner of the current screen
    // This allows CameraX to automatically start/stop the camera based on lifecycle events like onStart/onStop
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        // factory creates the traditional Android View once
        // Compose automatically provides the current Context
        factory = { ctx ->
            // View that displays the camera preview
            val previewView = PreviewView(ctx)
            // Asynchronously obtains the camera provider
            // The provider manages camera use cases
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            // Runs when the provider is ready
            cameraProviderFuture.addListener({
                // Retrieve the actual camera provider
                val cameraProvider = cameraProviderFuture.get()

                // Creates a preview use case
                val preview = Preview.Builder().build().also {
                    // Connects camera output to the PreviewView surface
                    it.surfaceProvider = previewView.surfaceProvider
                }

                // Get the front camera if available else any camera
                val cameraSelector =
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.Builder().build()
                    }

                // Clears previous bindings
                cameraProvider.unbindAll()
                //  Bind the preview use case to lifecycle
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            }, ContextCompat.getMainExecutor(ctx)) // Run listener on main thread
            // Returns the View so Compose can display it
            previewView
        }
    )
}