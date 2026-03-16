package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs
import kotlin.math.hypot

class JotaroPointPose : PoseDefinition {
    override val name = "Jotaro Point Pose"

    override fun matches(frame: PoseFrame): Boolean {
        val lm = frame.landmarks
        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val rElbo = lm[14] ?: return false
        val rWris = lm[16] ?: return false
        val rHip = lm[24] ?: return false

        // Right hand: Pointing forward
        val pointingForward = rWris.z < (rShou.z - 0.50f)
        val armStraightOut = abs(rWris.x - rShou.x) < 0.08f

        // Shoulder tilt to avoid false positive with josuke's pose
        val shoulderTilt = abs(rShou.y - lShou.y)
        val notJosukeTilt = shoulderTilt < 0.12f

        // Torso sideways
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val torsoSideways = rBodyAngle < 55.0

        return pointingForward && armStraightOut && notJosukeTilt && torsoSideways
    }
}