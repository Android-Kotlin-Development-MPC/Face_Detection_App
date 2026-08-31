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
            leftEyePosition = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
                ?.let { PointF(it.x, it.y) },
            rightEyePosition = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
                ?.let { PointF(it.x, it.y) },
            headEulerAngleX = face.headEulerAngleX,
            headEulerAngleY = face.headEulerAngleY,
            headEulerAngleZ = face.headEulerAngleZ
        )
}