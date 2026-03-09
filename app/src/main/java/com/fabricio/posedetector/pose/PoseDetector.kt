package com.fabricio.posedetector.pose

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import androidx.core.graphics.createBitmap
import com.fabricio.posedetector.detection.filter.LandmarkerFilter
import com.fabricio.posedetector.detection.interpreter.PoseInterpreter
import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.detection.poses.HandUpPose
import com.fabricio.posedetector.detection.poses.JosukeHipPose
import com.fabricio.posedetector.detection.poses.KoichiPose
import com.fabricio.posedetector.util.PoseLogger
import com.google.mediapipe.framework.image.BitmapImageBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 Class that wraps MediaPipe's PoseLandmarker and handles:
 * - Receiving camera frames
 * - Converting them into MediaPipe-compatible images
 * - Running asynchronous pose detection
 * - Emitting raw landmark data to higher-level pose evaluation logic (later)
*/
class PoseDetector(context: Context) {
    // Internal mutable state that stores the most recently processed PoseFrame
    private val _currentFrame = MutableStateFlow<PoseFrame?>(null)
    // Public read-only stream exposing the latest detected PoseFrame
    // CameraScreen collects this state to receive updates and draw the skeleton overlay whenever a new frame is processed
    val currentFrame: StateFlow<PoseFrame?> = _currentFrame
    private val poseLandmarker : PoseLandmarker // Instance of the class that performs pose landmarks detection on images
    // Filters raw mediapipe landmarks based on visibility and converts them into our PoseFrame model
    private val filter = LandmarkerFilter()
    // Interprets a filtered PoseFrame and checks it against the pose registry
    private val interpreter = PoseInterpreter(
        listOf(
            HandUpPose(),
            JosukeHipPose(),
            KoichiPose()
        )
    )

    init {

        // Validates and builds the base options instance
        // setting the model path to a model asset file in the assets folder
        val baseOptions = BaseOptions.builder().setModelAssetPath("pose_landmarker_full.task").build()

        // Validates and builds the options for the pose landmarker
        // Uses the base options
        // Sets the running mode to a live stream of input data
        // Sets the result listener to receive detection results asynchronously for the live stream mode
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, input ->
                // Ensure the landmarks are being read, else stops processing
                if (result.landmarks().isEmpty()) return@setResultListener

                val rawLandmarks = result.landmarks()[0] // Retrieves the 33 pose landmarks

                // Filters the landmarks with low visibility and converts them into our PoseFrame model
                // If nothing passes the filter skips the frame
                val frame = filter.filter(rawLandmarks) ?: return@setResultListener

                // Update the current frame so any UI observing the StateFlow (such as the skeleton overlay) receives the latest landmarks
                _currentFrame.value = frame
                // Log landmark data for debugging and pose calibration
                PoseLogger.logFrame(frame)

                // Pases the filtered PoseFrame to the interpreter and checks if it matched a registered pose
                val matchedPose = interpreter.interpret(frame)

                // If any match log the name of the matched pose
                matchedPose?.let {
                    Log.d("PoseTrigger", "Matched pose: ${it.name}")
                }

            }
            .setErrorListener { error ->
                Log.d("Pose", error.message ?: "Error in the pose options")
            }
            .build()

        // Creates the MediaPipe PoseLandmarker instance using the configured options
        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    // Receives frames from CameraX, converts them into a MediaPipe compatible format and submits them for async detection
    fun analyze(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis() // Variable to add a timestamp to the ImageProxy
        val bitmap = createBitmap(imageProxy.width, imageProxy.height) // Creates a bitmap for the image

        // Copies the image into the bitmap
        imageProxy.use {
            bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
        }

        // Converts the Bitmap into a MediaPipe image container
        val mpImage = BitmapImageBuilder(bitmap).build()

        // Sends live image data to perform pose landmarks detection
        poseLandmarker.detectAsync(mpImage, frameTime)

        imageProxy.close() // Release the ImageProxy object once processing is done to avoid freezing
    }

}