package com.fabricio.posedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fabricio.posedetector.ui.CameraScreen
import com.fabricio.posedetector.ui.PermissionGate

class MainActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionGate {
                CameraScreen()
            }
        }
    }
}