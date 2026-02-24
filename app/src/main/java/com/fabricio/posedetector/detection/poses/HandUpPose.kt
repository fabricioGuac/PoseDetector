package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame

// Simple pose definition  where both wrists are above their respective shoulders
class HandUpPose : PoseDefinition {
    override val name = "HandsUp"

    override fun matches(frame: PoseFrame): Boolean {
        val leftShoulder = frame.landmarks[11] ?: return false
        val rightShoulder = frame.landmarks[12] ?: return false
        val leftWrist = frame.landmarks[15] ?: return false
        val rightWrist = frame.landmarks[16] ?: return false

        val leftUp = leftWrist.y < leftShoulder.y
        val rightUp = rightWrist.y < rightShoulder.y

        return leftUp && rightUp
    }
}