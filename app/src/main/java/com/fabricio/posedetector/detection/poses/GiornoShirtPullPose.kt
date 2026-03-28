package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.R
import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs

class GiornoShirtPullPose : PoseDefinition {
    override val name = "Giorno shirt pull pose"
    override val imageRes = R.drawable.giorno_shirt_pull_pose
    override val audioRes = R.raw.giorno_shirt_pull_pose

    override fun matches(frame: PoseFrame): Boolean {
        val lm = frame.landmarks
        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val lElbo = lm[13] ?: return false
        val rElbo = lm[14] ?: return false
        val lWris = lm[15] ?: return false
        val rWris = lm[16] ?: return false
        val lHip = lm[23] ?: return false

        // Right arm: The chest pull
        val rArmAngle = calculateAngle(rShou, rElbo, rWris)
        val rArmFolded = rArmAngle < 20.0

        // Right wrist: Should be at pectoral height and between the shoulders
        val rHandAtChestHeight = abs(rWris.x - rShou.x) < 0.1f
        val rHandCentered = rWris.y > lShou.y && rWris.y < (rShou.y + 0.05f)

        // Left arm: The hip shirt pull
        val lArmAngle = calculateAngle(lShou, lElbo, lWris)
        val lArmValid = lArmAngle in 90.0..135.0

        // Left Wrist: Should be near the left hip
        val lHandAtHipHeight = abs(lWris.x - lHip.x) < 0.15f
        val lHandAtHipSide = abs(lWris.y - lHip.y) < 0.1f

        return rArmFolded && rHandAtChestHeight && rHandCentered &&
                lArmValid && lHandAtHipHeight && lHandAtHipSide    }
}