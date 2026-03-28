package com.fabricio.posedetector.detection.poses

import android.util.Log
import com.fabricio.posedetector.R
import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.util.calculateAngle
import kotlin.math.abs

class JonathanHandFacePose : PoseDefinition {
    override val name = "Jonathan Hand Face Pose"
    override val imageRes = R.drawable.jonathan_hand_face_pose
    override val audioRes = R.raw.jonathan_hands_face_pose

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

        // Left arm: Folded sharply for hand to face proximity
        val lArmAngle = calculateAngle(lShou, lElbo, lWris)
        val lArmFolded   = lArmAngle in 30.0..90.0

        // Right arm: Down with slight angle
        val rArmAngle = calculateAngle(rShou, rElbo, rWris)
        val rArmStraight = rArmAngle in 150.0..172.0

        // Left elbow: tucked
        // Right arm: flared
        val lBodyAngle = calculateAngle(lElbo, lShou, lHip)
        val rBodyAngle = calculateAngle(rElbo, rShou, rHip)
        val lTucked = lBodyAngle < 40.0
        val rFlared = rBodyAngle in 35.0..80.0

        // Left wrist: Near the shoulder face area
        val handNearFace = abs(lWris.x - lShou.x) < 0.15f && abs(lWris.y - lShou.y) < 0.15f

        return lArmFolded && rArmStraight && lTucked && rFlared && handNearFace
    }
}