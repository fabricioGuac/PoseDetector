package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs

class JosephPose : PoseDefinition {
    override val name = "Joseph Pose"

    override fun matches(frame: PoseFrame): Boolean {
        val lm = frame.landmarks
        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val lElbo = lm[13] ?: return false
        val rElbo = lm[14] ?: return false
        val lHip = lm[23] ?: return false
        val rHip = lm[24] ?: return false
        val lWris = lm[15]
        val rWris = lm[16]

        // Body angles: Arms reaching up from the torso
        val lBodyAngle = calculateAngle(lElbo, lShou, lHip)
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val lBodyValid = lBodyAngle in 140.0..175.0
        val rBodyValid = rBodyAngle in 140.0..175.0

        // Rohan Exclusion Flag
        // If we CAN see the wrists, and they are close together, this is NOT Joseph
        if (lWris != null && rWris != null) {
            val handsTogether = abs(lWris.x - rWris.x) < 0.1f && abs(lWris.y - rWris.y) < 0.1f
            if (handsTogether) return false
        }

        // Shoulders: Ensures the shoulders are aligned in the z axis
        val alignmentValid = abs(lShou.z - rShou.z) < 0.2f

        return lBodyValid && rBodyValid  && alignmentValid
    }
}