package com.fabricio.posedetector.detection.model

/**
 * Internal domain model representing a single body landmark
 *
 * This model is intentionally decoupled from mediapipe's NormaizedLamdmark to protect
 * the rest of the app from external API changes
 *
 * Coordinates are normalized: x being horizontal (0 = left, 1 = right), y being vertical (0 = top, 1 = bottom)
 * z being depth (negative = closer) and visibility is a confidence score of how likely the landmark is
 * visible in the frame
 */
data class Landmark (
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)