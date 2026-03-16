package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs

class RohanHandsUpPose : PoseDefinition {
    override val name = "Rohan hands above head pose"

    override fun matches(frame: PoseFrame): Boolean {
        val lm = frame.landmarks
        val nose = lm[0] ?: return false
        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val lElbo = lm[13] ?: return false
        val rElbo = lm[14] ?: return false
        val lWris = lm[15] ?: return false
        val rWris = lm[16] ?: return false
        val lHip = lm[23] ?: return false
        val rHip = lm[24] ?: return false

        // Arm angles: Asymmetric angles
        val lArmAngle = calculateAngle(lShou,lElbo,lWris)
        val rArmAngle = calculateAngle(rShou,rElbo,rWris)
        val armAnglesValid = lArmAngle in 135.0..175.0 && rArmAngle in 70.0..115.0

        // Body Angles: Arms raised relative to torso
        val lBodyAngle = calculateAngle(lElbo, lShou, lHip)
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val bodyValid = lBodyAngle in 155.0..185.0 && rBodyAngle in 135.0..175.0


        // Hands: Together  and above the nose (head)
        val handsTogether = abs(lWris.x - rWris.x) < 0.08f && abs(lWris.y - rWris.y) < 0.12f
        val handsAboveHead = lWris.x < nose.x && rWris.x < nose.x

        return armAnglesValid && bodyValid && handsTogether && handsAboveHead
    }
}