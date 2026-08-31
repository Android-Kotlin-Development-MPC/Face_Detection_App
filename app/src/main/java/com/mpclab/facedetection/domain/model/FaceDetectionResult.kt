package com.mpclab.facedetection.domain.model

import android.graphics.PointF
import android.graphics.RectF

data class FaceDetectionResult(
    val boundingBox: RectF,
    val smilingProbability: Float?,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val leftEyePosition: PointF?,
    val rightEyePosition: PointF?,
    val headEulerAngleX: Float?,
    val headEulerAngleY: Float?,
    val headEulerAngleZ: Float?
) {
    val isSmiling: Boolean get() = (smilingProbability ?: 0f) >= SMILING_THRESHOLD

    val isLeftEyeOpen: Boolean get() = (leftEyeOpenProbability ?: 0f) >= EYE_OPEN_THRESHOLD

    val isRightEyeOpen: Boolean get() = (rightEyeOpenProbability ?: 0f) >= EYE_OPEN_THRESHOLD

    companion object {
        const val SMILING_THRESHOLD = 0.7f
        const val EYE_OPEN_THRESHOLD = 0.3f
    }
}