package com.fabricio.posedetector.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fabricio.posedetector.detection.model.PoseFrame

/**
 * Function to draw a skeleton overlay using the landmarks contained in a PoseFrame
 * Landmarks are provided in normalized MediaPipe coordinates (0.0-1.0),
 * so the have to be converted to screen coordinates before drawing
 **/
@Composable
fun PoseOverlay(frame: PoseFrame, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Canvas dimensions used to convert normalized landmarks coordinates into actual screen pixel positions
        val width = size.width
        val height = size.height

        // Landmark index pairs defining the skeleton bones corresponding to MediaPose landmark indices
        val connections = listOf(
            11 to 13, 13 to 15, // left arm
            12 to 14, 14 to 16, // right arm
            23 to 25, 25 to 27, // left leg
            24 to 26, 26 to 28, // right leg
            11 to 12, 23 to 24, 11 to 23, 12 to 24 // torso
        )


        connections.forEach { (startIdx, endIdx) ->
            val startLm = frame.landmarks[startIdx]
            val endLm = frame.landmarks[endIdx]

            // Only draw "bone" if both points exist and have decent visibility
            if (startLm != null && endLm != null) {
                drawLine(
                    color = Color.Cyan.copy(alpha = 0.6f),
                    // Coverts normalized landmark coordinates to screen space
                    // Due to the x and y being flipped currently we swap them accordingly and
                    // flip the horizontal axis using (1 - y)
                    start = Offset((1f - startLm.y) * width, startLm.x * height),
                    end = Offset((1f - endLm.y) * width, endLm.x * height),
                    strokeWidth = 5f
                )
            }
        }

        // Draw the "Joints" (Landmarks)
        frame.landmarks.forEach { (index, landmark) ->
            // Convert normalized landmark coordinates into screen coordinates
            // using the same mapping used for drawing the bones
            val cx = (1f - landmark.y) * width
            val cy = landmark.x * height

            drawCircle(
                // High visibility landmarks are drawn green, lower confidence ones yellow
                color = if (landmark.visibility > 0.7f) Color.Green else Color.Yellow,
                radius = 8f,
                center = Offset(cx, cy)
            )
        }
    }
}