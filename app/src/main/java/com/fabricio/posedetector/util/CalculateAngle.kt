package com.fabricio.posedetector.util

import com.fabricio.posedetector.detection.model.Landmark
import kotlin.math.abs
import kotlin.math.atan2

// Helper function to calculate the exact angle of a joint (b)
fun calculateAngle(a: Landmark, b: Landmark, c: Landmark): Double {
    // atan2 returns the orientation (in radians) of a vector relative to the horizontal axis
    // Here we compute:
    //    - direction from joint B to C (one limb segment)
    //    - direction from joint B to A (the other limb segment)
    // Subtracting these orientations gives the angle between the two segments
    // The result is then converted from radians to degrees
    var angle = Math.toDegrees(
        atan2(c.y - b.y, c.x - b.x).toDouble() - atan2(a.y - b.y, a.x - b.x).toDouble()
    )
    // Absolute value because we only care how much the joint bends
    angle = abs(angle)
    // If the angle exceeds 180 take the smaller interior angle
    return if (angle > 180.0) 360.0 - angle else angle
}
