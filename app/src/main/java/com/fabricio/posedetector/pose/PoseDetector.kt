package com.fabricio.posedetector.pose

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import androidx.core.graphics.createBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder

/**
 Class that wraps MediaPipe's PoseLandmarker and handles:
 * - Receiving camera frames
 * - Converting them into MediaPipe-compatible images
 * - Running asynchronous pose detection
 * - Emitting raw landmark data to higher-level pose evaluation logic (later)
*/
class PoseDetector(context: Context) {
    private val poseLandmarker : PoseLandmarker // Instance of the class that performs pose landmarks detection on images
    private var handsUpPreviously = false // Flag to keep track of the last hands up state for the test pose

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
                // TODO: Move pose interpretation logic into a separate class
                // Simple arms up detection for debugging

                // Ensures the landmarks are being read
                if (result.landmarks().isNotEmpty()) {

                    val firstPose = result.landmarks()[0] // Retrieves the 33 pose landmarks

                    // Ensures the expected landmark indices exist (right wrist index = 16)
                    if (firstPose.size >= 17) {

                        // Retrieves the landmarks needed for the test pose
                        val leftShoulder = firstPose[11]
                        val rightShoulder = firstPose[12]
                        val leftWrist = firstPose[15]
                        val rightWrist = firstPose[16]

                        // Check the positions (0 is top and 1 bottom of the image respectively)
                        val leftHandUp = leftWrist.y() < leftShoulder.y()
                        val rightHandUp = rightWrist.y() < rightShoulder.y()

                        // If both hands are up and that it is not repeating logs from the last read logs the debug message
                        val handsUpNow = leftHandUp && rightHandUp
                        if (handsUpNow && !handsUpPreviously) {
                            Log.d("PoseTrigger", "YAAAAAAAAAAAAAYYYYYYY!")
                        }
                        // Updates the flag
                        handsUpPreviously = handsUpNow
                        Log.d("DEBUG",
                            "LW:${leftWrist.y()} LS:${leftShoulder.y()} RW:${rightWrist.y()} RS:${rightShoulder.y()}")

                    }
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