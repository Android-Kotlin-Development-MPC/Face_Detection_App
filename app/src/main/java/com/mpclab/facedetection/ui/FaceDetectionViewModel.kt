package com.mpclab.facedetection.ui

import androidx.lifecycle.ViewModel
import com.mpclab.facedetection.domain.model.FaceDetectionResult
import com.mpclab.facedetection.util.FaceStatusFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FaceDetectionUiState(
    val isReady: Boolean = false,
    val faceCount: Int = 0,
    val faces: List<FaceDetectionResult> = emptyList(),
    val statusText: String = FaceStatusFormatter.NO_FACES,
    val errorMessage: String? = null
)

class FaceDetectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FaceDetectionUiState())
    val uiState: StateFlow<FaceDetectionUiState> = _uiState.asStateFlow()

    fun onFacesDetected(faces: List<FaceDetectionResult>) {
        _uiState.value = FaceDetectionUiState(
            isReady = true,
            faceCount = faces.size,
            faces = faces,
            statusText = FaceStatusFormatter.format(faces)
        )
    }

    fun onCameraError(message: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun onCameraReady() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}