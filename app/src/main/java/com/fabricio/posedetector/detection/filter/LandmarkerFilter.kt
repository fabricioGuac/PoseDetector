package com.fabricio.posedetector.detection.filter

import com.fabricio.posedetector.detection.model.Landmark
import com.fabricio.posedetector.detection.model.PoseFrame
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * Class responsible for filtering the raw mediapipe landmarks based on visibility threshold
 * and converting them into our PoseFrame model
 */
class LandmarkerFilter(private val minVisibility: Float = 0.6f) {
    // Function to return a PoseFrame with only the visible enough landmarks
    fun filter(rawLandmarks: List<NormalizedLandmark>): PoseFrame? {
        val visibleLandmarks = rawLandmarks
            .mapIndexed { index, lm -> index to lm } // Attaches the landmark index to each landmark
            // Filters our landmarks with low visibility, if visibility is not preset it treats it as 0f
            .filter { (_,lm) ->
                val visibility = lm.visibility().orElse(0f)
                visibility >= minVisibility
            }
            // Converts the filtered landmarks into our Landmark model
            .associate { (index, lm) ->
                index to Landmark(
                    x = lm.x(),
                    y = lm.y(),
                    z = lm.z(),
                    visibility = lm.visibility().orElse(0f)
                )
            }

        // If no landmark passed the filter return null, otherwise wrap them inside a PoseFrame for interpretation
        return if (visibleLandmarks.isEmpty()) null else PoseFrame(visibleLandmarks)
    }
}