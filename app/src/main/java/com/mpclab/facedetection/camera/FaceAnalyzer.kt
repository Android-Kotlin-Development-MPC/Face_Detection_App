package com.mpclab.facedetection.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.mpclab.facedetection.domain.model.FaceDetectionResult

class FaceAnalyzer(
    private val detector: FaceDetector,
    private val isFrontCamera: Boolean,
    private val onImageSourceInfo: (imageWidth: Int, imageHeight: Int, isImageFlipped: Boolean) -> Unit,
    private val onFacesDetected: (List<FaceDetectionResult>) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile
    private var isProcessing = false

    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing || imageProxy.image == null) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image!!
        isProcessing = true

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val isRotated = rotationDegrees == ROTATION_90 || rotationDegrees == ROTATION_270
        val uprightWidth = if (isRotated) imageProxy.height else imageProxy.width
        val uprightHeight = if (isRotated) imageProxy.width else imageProxy.height
        onImageSourceInfo(uprightWidth, uprightHeight, isFrontCamera)

        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                onFacesDetected(faces.map { FaceResultMapper.toDetectionResult(it) })
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Face detection failed", error)
            }
            .addOnCompleteListener {
                imageProxy.close()
                isProcessing = false
            }
    }

    private companion object {
        const val TAG = "FaceAnalyzer"
        const val ROTATION_90 = 90
        const val ROTATION_270 = 270
    }
}