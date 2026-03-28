package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.R
import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs

class KakyionPose : PoseDefinition {
    override val name = "Kakyion pose"
    override val imageRes = R.drawable.kakyion_pose
    override val audioRes = R.raw.kakyion_pose

    override fun matches(frame: PoseFrame): Boolean {
        val lm = frame.landmarks
        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val lElbo = lm[13] ?: return false
        val rElbo = lm[14] ?: return false
        val lWris = lm[15] ?: return false
        val rWris = lm[16] ?: return false
        val lHip  = lm[23] ?: return false
        val rHip  = lm[24] ?: return false

        // Left arm: Folded across the mid torso
        val lArmAngle = calculateAngle(lShou, lElbo, lWris)
        val lArmFolded = lArmAngle < 60.0

        // Right arm: Bent pointing up
        val rArmAngle = calculateAngle(rShou, rElbo, rWris)
        val rArmBent = rArmAngle in 65.0..115.0

        //Torso orientation: Torso slightly angled
        val lBodyAngle = calculateAngle(lElbo, lShou, lHip)
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val lBodyValid = lBodyAngle in 25.0..55.0
        val rBodyValid = rBodyAngle in 30.0..65.0

        // Shoulders at the same height
        val shouldersAligned = abs(lShou.x - rShou.x) < 0.1

        return lArmFolded && rArmBent &&
                lBodyValid && rBodyValid &&
                shouldersAligned
    }
}