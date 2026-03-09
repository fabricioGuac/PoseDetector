package com.fabricio.posedetector.detection.interpreter

/**
 * Class responsible ot validating a detected pose is held steadily for a minimum duration
 *
 * This prevents accidental triggers caused by frame noise or quick transitions between poses
 * by requiring poses to remain valid for a continuous period of time while allowing small interruptions
 */
class PoseHoldValidator(
    private val holdDurationMs: Long = 2000L, // Minimum time a pose must remain valid before triggering
    private val toleranceMs: Long = 250L // Allowed interruption window
    ) {
    private var holdStartTime: Long? = null // Timestamp whn the pose was first detected
    private var lastMatchTime: Long? = null // Timestamp of the most recent frame the pose matched
    private var confirmed = false // Ensures the pose only triggers once per hold

    // Function to update the validator with the latest pse match result
    fun update(matches: Boolean): Boolean {
        val now = System.currentTimeMillis()

        if (matches) {
            // If this is the first matching frame, record the start time
            if (holdStartTime == null) {
                holdStartTime = now
            }
            // Update the timestamp of the last matching frame
            lastMatchTime = now

            // If the pose has been held long enough and hasn't triggered yet
            // mark it as confirmed and emit a trigger signal
            if ( !confirmed && now - holdStartTime!! >= holdDurationMs) {
                confirmed = true
                return true
            }
        } else {
            // If the pose has been missing longer than the allowed tolerance, consider hold broken and reset validator
            if (lastMatchTime == null || now - lastMatchTime!! > toleranceMs) {
                reset()
            }
        }
        return false
    }
    // Helper function to reset the hold tracking state when the pose is lost for too long
    private fun reset() {
        holdStartTime = null
        lastMatchTime = null
        confirmed = false
    }

}