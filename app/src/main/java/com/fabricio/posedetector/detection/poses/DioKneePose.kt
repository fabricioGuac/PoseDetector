package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle

class DioKneePose : PoseDefinition {
    override val name = "Dio knee up pose"

    override fun matches(frame: PoseFrame): Boolean {

        val lm = frame.landmarks

        val lShou = lm[11] ?: return false
        val rShou = lm[12] ?: return false
        val lElbo = lm[13] ?: return false
        val rElbo = lm[14] ?: return false
        val lWris = lm[15] ?: return false
        val rWris = lm[16] ?: return false
        val rHip  = lm[24] ?: return false
        val rKnee = lm[26] ?: return false


        // Left arm: Folded in a small angle
        val lArmAngle = calculateAngle(lShou, lElbo, lWris)
        val lArmValid = lArmAngle < 20.0

        // Right arm: Extended in a straight dramatic line
        val rArmAngle = calculateAngle(rShou, rElbo, rWris)
        val rArmValid = rArmAngle > 160.0


        // Right knee: Lifted relative to right hip
        val kneeRaised = (rHip.x - rKnee.x) > 0.05


        return lArmValid &&
                rArmValid &&
                kneeRaised
    }
}