package com.fabricio.posedetector.detection.poses

import com.fabricio.posedetector.detection.model.PoseFrame

/**
 * Interface to define what a detectable pose looks like
 *
 * Each pose implementation defines:
 * 1) A unique name
 * 2) The logic required to determine if a given PoseFrame matches it
 *
 * This allows the PoseInterpreter to evaluate multiple poses ina clean and extensible way
 */
interface PoseDefinition {
    val name: String
    // Returns true if the given frame satisfies the geometric conditions for the pose
    fun matches(frame: PoseFrame): Boolean
}