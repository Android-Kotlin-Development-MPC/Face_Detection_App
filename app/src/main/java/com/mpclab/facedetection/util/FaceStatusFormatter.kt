package com.mpclab.facedetection.util

import com.mpclab.facedetection.domain.model.FaceDetectionResult
import kotlin.math.roundToInt

object FaceStatusFormatter {

    const val NO_FACES = "No face detected — move into view"

    fun format(faces: List<FaceDetectionResult>): String {
        if (faces.isEmpty()) return NO_FACES
        return buildString {
            append("Faces detected: ").append(faces.size)
            faces.forEachIndexed { index, face ->
                append("\n\nFace ").append(index + 1)
                append("\n  Smiling: ")
                append(formatSmile(face))
                append("\n  Left eye: ").append(formatEye(face.leftEyeOpenProbability))
                append("\n  Right eye: ").append(formatEye(face.rightEyeOpenProbability))
            }
        }
    }

    fun formatFaceSummary(face: FaceDetectionResult): String =
        "Smiling: ${percent(face.smilingProbability)}, " +
            "Left Eye: ${formatEye(face.leftEyeOpenProbability)}, " +
            "Right Eye: ${formatEye(face.rightEyeOpenProbability)}"

    private fun formatSmile(face: FaceDetectionResult): String {
        val probability = face.smilingProbability ?: return "Unknown"
        return "${percent(probability)} (${if (face.isSmiling) "smiling" else "neutral"})"
    }

    private fun formatEye(probability: Float?): String = when {
        probability == null -> "Unknown"
        probability >= FaceDetectionResult.EYE_OPEN_THRESHOLD ->
            "Open (${percent(probability)})"
        else -> "Closed (${percent(probability)})"
    }

    private fun percent(probability: Float?): String =
        probability?.let { "${(it * 100).roundToInt()}%" } ?: "n/a"
}