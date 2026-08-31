package com.mpclab.facedetection.domain.detector

import com.google.mlkit.vision.face.FaceDetectorOptions

object FaceDetectorFactory {

    fun createDefaultOptions(): FaceDetectorOptions =
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.15f)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build()
}