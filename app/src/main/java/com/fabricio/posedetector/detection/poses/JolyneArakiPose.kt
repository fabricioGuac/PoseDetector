package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle

class JolyneArakiPose : PoseDefinition {
    override val name = "Jolyne Araki Pose"

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

        // Left arm: Folded sharply for hand to face contact
        val lArmAngle = calculateAngle(lShou, lElbo, lWris)
        val lArmFolded = lArmAngle in 25.0..65.0
        // Left wrist: Above shoulder (face)
        val lWristHigh = lWris.x < lShou.x

        // Right arm: Across the body
        val midShoulder = (lShou.y + rShou.y) / 2f
        val rArmAcross = rWris.y < midShoulder

        // Torso orientation: Strictly upright
        // Using the Elbow-Shoulder-Hip relationship to ensure the arm isn't flared out
        val lBodyAngle = calculateAngle(lElbo, lShou, lHip)
        val lBodyValid = lBodyAngle < 25.0
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val rBodyValid = rBodyAngle < 20.0


        return lArmFolded && rArmAcross &&
                lWristHigh  &&
                lBodyValid && rBodyValid
    }
}