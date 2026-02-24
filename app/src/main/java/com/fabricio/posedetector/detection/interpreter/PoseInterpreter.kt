package com.fabricio.posedetector.detection.interpreter

import com.fabricio.posedetector.detection.model.PoseFrame
import com.fabricio.posedetector.detection.poses.PoseDefinition

/** Class responsible for evaluating a processed PoseFrame against a list of
 * PoseDefinition implementations and returning the first matching pose
  */
class PoseInterpreter(private val poses: List<PoseDefinition>) {
    // Function that iterates through the available poses and returns the pose
    // whose 'matches()' function returns true, if no match returns null
    fun interpret(frame: PoseFrame) : PoseDefinition? {
        return poses.firstOrNull {it.matches((frame))}
    }
}