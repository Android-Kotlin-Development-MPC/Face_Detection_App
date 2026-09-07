package com.mpclab.facedetection.camera

import android.graphics.PointF
import android.graphics.RectF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import com.mpclab.facedetection.domain.model.FaceDetectionResult

object FaceResultMapper {

    fun toDetectionResult(face: Face): FaceDetectionResult =
        FaceDetectionResult(
            boundingBox = RectF(
                face.boundingBox.left.toFloat(),
                face.boundingBox.top.toFloat(),
                face.boundingBox.right.toFloat(),
                face.boundingBox.bottom.toFloat()
            ),
            smilingProbability = face.smilingProbability,
            leftEyeOpenProbability = face.leftEyeOpenProbability,
            rightEyeOpenProbability = face.rightEyeOpenProbability,
            leftEyePosition = face.landmark(FaceLandmark.LEFT_EYE),
            rightEyePosition = face.landmark(FaceLandmark.RIGHT_EYE),
            noseBasePosition = face.landmark(FaceLandmark.NOSE_BASE),
            mouthLeftPosition = face.landmark(FaceLandmark.MOUTH_LEFT),
            mouthRightPosition = face.landmark(FaceLandmark.MOUTH_RIGHT),
            mouthBottomPosition = face.landmark(FaceLandmark.MOUTH_BOTTOM),
            leftEarPosition = face.landmark(FaceLandmark.LEFT_EAR),
            rightEarPosition = face.landmark(FaceLandmark.RIGHT_EAR),
            headEulerAngleX = face.headEulerAngleX,
            headEulerAngleY = face.headEulerAngleY,
            headEulerAngleZ = face.headEulerAngleZ
        )

    private fun Face.landmark(landmarkType: Int): PointF? =
        getLandmark(landmarkType)?.position?.let { PointF(it.x, it.y) }
}