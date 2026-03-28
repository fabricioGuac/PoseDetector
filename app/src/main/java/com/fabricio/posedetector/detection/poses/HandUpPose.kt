package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.R
import com.fabricio.posedetector.detection.model.PoseFrame

// Simple pose definition  where both wrists are above their respective shoulders
// NOTE:
// Landmark coordinates currently appear rotated relative to the on-screen preview
// In practice this means the "vertical" comparison behaves as if X and Y are swapped
// As a temporary workaround we compare the X values instead of Y
//
// This likely relates to camera sensor orientation or preview rotation,
// but the exact cause has not been fully verified yet.
class HandUpPose : PoseDefinition {
    override val name = "HandsUp"
    override val imageRes = R.drawable.dio_knee_pose
    override val audioRes = R.raw.dio_knee_pose

    override fun matches(frame: PoseFrame): Boolean {
        val leftShoulder = frame.landmarks[11] ?: return false
        val rightShoulder = frame.landmarks[12] ?: return false
        val leftWrist = frame.landmarks[15] ?: return false
        val rightWrist = frame.landmarks[16] ?: return false

        val leftUp = leftWrist.x < leftShoulder.x
        val rightUp = rightWrist.x < rightShoulder.x

        return leftUp && rightUp
    }
}