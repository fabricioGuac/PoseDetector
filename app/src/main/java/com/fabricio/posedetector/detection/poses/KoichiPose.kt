package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle

class KoichiPose : PoseDefinition {

    override val name = "Koichi pose"

    override fun matches(frame: PoseFrame): Boolean {
        val lm = frame.landmarks
        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val lElbo = lm[13] ?: return false
        val rElbo = lm[14] ?: return false
        val lWris = lm[15] ?: return false
        val rWris = lm[16] ?: return false
        val lHip = lm[23] ?: return false
        val rHip = lm[24] ?: return false


        // Left arm: elbow bent roughly at a right angle (~90°)
        // creating the characteristic lifted arm position
        val lArmAngle = calculateAngle(lShou, lElbo, lWris)
        val lArmValid = lArmAngle in 75.0..105.0

        // Right arm: more open arm position with a wider angle giving the asymmetric look
        val rArmAngle = calculateAngle(rShou, rElbo, rWris)
        val rArmValid = rArmAngle in 65.0..163.0

        // Left side body angle: ensures the arms is lifted away from the torso
        val lBodyAngle = calculateAngle(lElbo, lShou, lHip)
        val lBodyValid = lBodyAngle in 55.0..85.0

        // Right side body angle: ensures arm is raused relative to the torso
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val rBodyValid = rBodyAngle in 55.0..85.0

        // Body orientation: ensures the body is slightly rotated
        // The left shoulder being closer to the camera indicates the pose is facing the right direction
        val orientationValid = lShou.z < rShou.z

        return lArmValid && rArmValid &&
                lBodyValid && rBodyValid && orientationValid
    }
}