package com.fabricio.posedetector.util

import android.util.Log
import com.fabricio.posedetector.detection.model.PoseFrame

/**
 * Utility used during pose development to log landmark coordinates and joint angles
 * helping understand how MediaPipe interprets specific poses and makes it easier to tune
 * pose detection thresholds
 */
object PoseLogger {

    private var lastLogged = 0L // Last log timestamp
    private const val LOG_INTERVAL_MS = 2000L // Minimum time between logs to avoid flooding Logcat while processing frames

    // Helper function to log diverse metadata from the frame landmarks
    fun logFrame(frame: PoseFrame) {
        val now = System.currentTimeMillis()
        if (now - lastLogged < LOG_INTERVAL_MS) return // Avoid logging before the interval
        lastLogged = now

        val landmarks = frame.landmarks

        val logString = buildString {
            append("\n--- POSE REFERENCE SNAPSHOT ---")

            // COORDINATES
            // We log 11-28 (Shoulders to Ankles)
            append("\n[LANDMARKS X, Y, Z]")
            for (id in 11..28) {
                landmarks[id]?.let {
                    append("\nID %2d: x=%.3f, y=%.3f, z=%.3f".format(id, it.x, it.y, it.z))
                }
            }

            // ANGLES
            append("\n\n[JOINT ANGLES]")
            val jointTriples = listOf(
                "L-Arm " to Triple(11, 13, 15),
                "R-Arm " to Triple(12, 14, 16),
                "L-Leg " to Triple(23, 25, 27),
                "R-Leg " to Triple(24, 26, 28),
                "L-Body" to Triple(13, 11, 23), // Elbow-Shoulder-Hip
                "R-Body" to Triple(14, 12, 24)
            )

            jointTriples.forEach { (label, triple) ->
                val (a, b, c) = triple
                val la = landmarks[a]
                val lb = landmarks[b]
                val lc = landmarks[c]
                if (la != null && lb != null && lc != null) {
                    val angle = calculateAngle(la, lb, lc)
                    append("\n%s (%d-%d-%d): %.2f°".format(label, a, b, c, angle))
                }
            }
            append("\n----------------------------------")
        }

        Log.d("PoseLogger", logString)
    }
}