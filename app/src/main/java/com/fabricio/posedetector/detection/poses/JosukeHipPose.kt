package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs
class JosukeHipPose : PoseDefinition {
    override val name = "Josuke hip pose"

override fun matches(frame: PoseFrame): Boolean {
    val lm = frame.landmarks
    val lShou = lm[11] ?: return false
    val rShou = lm[12] ?: return false
    val lElbo = lm[13] ?: return false
    val rElbo = lm[14] ?: return false
    val lWris = lm[15] ?: return false
    val rWris = lm[16] ?: return false

    // Torso tilt: right shoulder should be lower than the left
    val shoulderTilt = (rShou.y - lShou.y) > 0.15f

    // Right arm: arm is tightly folded so the elbow angle is very small
    val rArmFolded = calculateAngle(rShou, rElbo, rWris) < 25.0

    // Hand stays horizontally close to the right shoulder (near the pectoral area)
    val rHandInPectX = abs(rWris.x - rShou.x) < 0.1f

    // Hand is vertically at shoulder level
    val rHandInPectY = abs(rWris.y - rShou.y) < 0.1f

    // Hand is pushed forward relative to the torso (using Z depth from MediaPipe)
    val rHandForward = rWris.z < (rShou.z - 0.25f)

    // Left arm: arm rests around mid torso at ~45° elbow angle
    val lArmAngle = calculateAngle(lShou, lElbo, lWris)
    val lArmValid = lArmAngle in 30.0..65.0

    // Left hand is slightly closer to the torso than the left shoulder
    val lHandIn = lWris.y < lShou.y + 0.05f

    return shoulderTilt && rArmFolded && rHandInPectX &&
            rHandInPectY && rHandForward && lArmValid && lHandIn
}
}