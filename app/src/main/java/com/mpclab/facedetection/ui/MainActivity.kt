package com.mpclab.facedetection.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.mpclab.facedetection.R
import com.mpclab.facedetection.camera.FaceAnalyzer
import com.mpclab.facedetection.databinding.ActivityMainBinding
import com.mpclab.facedetection.domain.detector.FaceDetectorFactory
import com.mpclab.facedetection.domain.model.FaceDetectionResult
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: FaceDetectionViewModel by viewModels()

    private var faceDetector: FaceDetector? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startFaceDetection()
            } else {
                viewModel.onCameraError(getString(R.string.permission_denied))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        observeViewModel()

        if (hasCameraPermission()) {
            startFaceDetection()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
        cameraProvider = null
        faceDetector?.close()
        faceDetector = null
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.statusText.text = state.statusText
                    binding.errorText.isVisible = state.errorMessage != null
                    state.errorMessage?.let { binding.errorText.text = it }
                }
            }
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun startFaceDetection() {
        val detector = FaceDetection.getClient(FaceDetectorFactory.createDefaultOptions())
        faceDetector = detector

        val executor = Executors.newSingleThreadExecutor()
        cameraExecutor = executor

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val isFrontCamera = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
            )
            .build()

        val targetRotation = binding.previewView.display.rotation

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(targetRotation)
            .build()
            .also { preview ->
                preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(targetRotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(
                    executor,
                    FaceAnalyzer(
                        detector = detector,
                        isFrontCamera = isFrontCamera,
                        onImageSourceInfo = binding.graphicOverlay::setImageSourceInfo,
                        onFacesDetected = this::onFacesDetected
                    )
                )
            }

        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener(
            {
                runCatching { providerFuture.get() }
                    .onSuccess { provider ->
                        cameraProvider = provider
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        viewModel.onCameraReady()
                    }
                    .onFailure { error ->
                        viewModel.onCameraError(
                            error.message ?: getString(R.string.camera_start_error)
                        )
                    }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun onFacesDetected(faces: List<FaceDetectionResult>) {
        binding.graphicOverlay.setFaces(faces)
        viewModel.onFacesDetected(faces)
    }
}