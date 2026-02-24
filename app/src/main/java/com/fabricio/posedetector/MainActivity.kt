package com.fabricio.posedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fabricio.posedetector.pose.PoseDetector
import com.fabricio.posedetector.camera.CameraScreen
import com.fabricio.posedetector.camera.PermissionGate

class MainActivity : ComponentActivity(){

    // Single PoseDetector instance scoped to this Activity's lifecycle
    // Initialized in onCreate() once a valid Context is available
    private lateinit var poseDetector: PoseDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creates the MediaPipe based pose detector used by the camera pipeline
        poseDetector = PoseDetector(this)

        setContent {
            PermissionGate {
                CameraScreen(poseDetector)
            }
        }
    }
}