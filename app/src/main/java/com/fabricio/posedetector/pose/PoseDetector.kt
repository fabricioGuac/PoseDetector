package com.fabricio.posedetector.pose

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.SoundPool
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
import com.fabricio.posedetector.detection.poses.DioKneePose
import com.fabricio.posedetector.detection.poses.GiornoShirtPullPose
import com.fabricio.posedetector.detection.poses.JolyneArakiPose
import com.fabricio.posedetector.detection.poses.JonathanHandFacePose
import com.fabricio.posedetector.detection.poses.JosephPose
import com.fabricio.posedetector.detection.poses.JosukeHipPose
import com.fabricio.posedetector.detection.poses.JotaroPointPose
import com.fabricio.posedetector.detection.poses.KakyionPose
import com.fabricio.posedetector.detection.poses.KoichiPose
import com.fabricio.posedetector.detection.poses.PoseDefinition
import com.fabricio.posedetector.detection.poses.RohanHandsUpPose
import com.fabricio.posedetector.util.PoseLogger
import com.google.mediapipe.framework.image.BitmapImageBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.core.net.toUri

/**
 Class that wraps MediaPipe's PoseLandmarker and handles:
 * - Receiving camera frames
 * - Converting them into MediaPipe-compatible images
 * - Running asynchronous pose detection
 * - Emitting raw landmark data to higher-level pose evaluation logic (later)
*/
class PoseDetector(private val context: Context) {
    // Full pose list
    private val poses = listOf(
        JosukeHipPose(),
        KoichiPose(),
        JolyneArakiPose(),
        JotaroPointPose(),
        JosephPose(),
        JonathanHandFacePose(),
        RohanHandsUpPose(),
        GiornoShirtPullPose(),
        KakyionPose(),
        DioKneePose()
    )
    // Internal mutable state that stores the most recently processed PoseFrame
    private val _currentFrame = MutableStateFlow<PoseFrame?>(null)
    // Public read-only stream exposing the latest detected PoseFrame
    // CameraScreen collects this state to receive updates and draw the skeleton overlay whenever a new frame is processed
    val currentFrame: StateFlow<PoseFrame?> = _currentFrame

    // Emits pose detection events when a pose is confirmed
    private val _poseEvent = MutableSharedFlow<PoseDefinition>(extraBufferCapacity = 1)
    // Public read-only stream of pose detection events
    // Collected by the UI to trigger side effects like audio and overlays
    val poseEvent = _poseEvent.asSharedFlow()

    private val poseLandmarker : PoseLandmarker // Instance of the class that performs pose landmarks detection on images
    // Filters raw mediapipe landmarks based on visibility and converts them into our PoseFrame model
    private val filter = LandmarkerFilter()
    // Interprets a filtered PoseFrame and checks it against the pose registry
    private val interpreter = PoseInterpreter(poses)

    // Attributes defining it will be used as a trigger event in a game
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    // SoundPool instance used to preload and play short audio clips with low latency
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(attributes)
        .build()

    // Maps for the audio resource ids to the SoundPool sound ID and the duration in milliseconds
    private val soundMap = mutableMapOf<Int, Int>()
    private val durationMap = mutableMapOf<Int, Long>()

    init {

        // Preload all pose audio clips into SoundPool and cache their durations
        poses.forEach { pose ->
            soundMap[pose.audioRes] = soundPool.load(context, pose.audioRes, 1)
            durationMap[pose.audioRes] = getAudioDuration(pose.audioRes)
        }

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
//                PoseLogger.logFrame(frame)

                // Pases the filtered PoseFrame to the interpreter and checks if it matched a registered pose
                val matchedPose = interpreter.interpret(frame)

                // If any match log the name of the matched pose
                // If any match emmit the matched pose
                matchedPose?.let {
                    Log.d("PoseTrigger", "Matched pose: ${it.name}")
                    _poseEvent.tryEmit(it)
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

    // Plays the sound associated with a pose and returns its duration in milliseconds
    fun playPoseSound(resId: Int): Long {
        val soundId = soundMap[resId] ?: return 0L
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        return durationMap[resId] ?: 0L
    }

    // Extracts audio duration from a raw resource (used to control UI overlay timing)
    private fun getAudioDuration(resId: Int): Long {
        val retriever = MediaMetadataRetriever()

        // The URI for a raw resource
        val uri = "android.resource://${context.packageName}/$resId".toUri()

        return try {
            retriever.setDataSource(context, uri)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            time?.toLong() ?: 2000L
        } catch (e: Exception) {
            Log.e("AudioError", "Could not read duration for $resId: ${e.message}")
            2000L // Fallback to 2 seconds if the file is unreadable
        }
    }
}