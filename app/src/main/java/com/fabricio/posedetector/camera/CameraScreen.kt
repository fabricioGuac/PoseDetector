package com.fabricio.posedetector.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fabricio.posedetector.detection.poses.PoseDefinition
import com.fabricio.posedetector.pose.PoseDetector
import com.fabricio.posedetector.ui.PoseOverlay
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

// Creates a traditional android camera preview and binds it to the app lifecycle so it turns on and off automatically
@Composable
fun CameraScreen(poseDetector: PoseDetector){
    // Gets the LifeCycleOwner of the current screen
    // This allows CameraX to automatically start/stop the camera based on lifecycle events like onStart/onStop
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Single background thread for image analysis, preventing heavy processing from blocking UI
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Collects the latest processed frame from PoseDetector as compose state
    // recomposing the screen to update the overlay real time each new frame
    val frame by poseDetector.currentFrame.collectAsState()

    // Holds the currently triggered pose
    var activePose by remember { mutableStateOf<PoseDefinition?>(null) }
    // Context needed to play the audio
    val context = LocalContext.current.applicationContext


    // Collects pose detection events emitted by PoseDetector
    LaunchedEffect(Unit) {
        poseDetector.poseEvent.collect { pose ->
            // Updates activePose to trigger UI response
            activePose = pose

            // Play the audio
            // Dynamically wait for the exact duration of the audio clip
            val duration = poseDetector.playPoseSound(pose.audioRes)
            delay(duration)

            // Clear the image once the time is up
            activePose = null
        }
    }

    // Blurs the component if the pose is active
    val blurModifier = if (activePose != null) Modifier.blur(40.dp) else Modifier

    // Box to allow stacking UI elements on top of each other
    Box(modifier = Modifier.fillMaxSize()){
        AndroidView(
            modifier = Modifier.fillMaxSize()
                .then(blurModifier),
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

                    // Receives individual frames from the camera for processing
                    // If frames arrive faster than they are processed drop the old and keep the most recent
                    // Outputs frames in RGBA format (required for bitmap conversion)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        // Analyser runs on background thread (cameraExecutor)
                        // Each frame is passed to PoseDetector for landmark detection
                        .also { analysis ->
                            analysis.setAnalyzer(
                                cameraExecutor
                            ) {imageProxy ->
                                poseDetector.analyze(imageProxy)
                            }
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
                        preview,
                        imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(ctx)) // Run listener on main thread
                // Returns the View so Compose can display it
                previewView
            }
        )
        // If a processed frame with pose landmarks is available pass it to the PoseOverlay
        // to render the skeleton on top of the preview
        frame?.let {
            PoseOverlay(
                frame = it,
                modifier = blurModifier
                )
        }

        // Dark overlay when pose is active
        if (activePose != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Displays the pose image overlay when a pose is active
        activePose?.let { pose ->
            Image(
                painter = painterResource(id = pose.imageRes),
                contentDescription = pose.name,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}