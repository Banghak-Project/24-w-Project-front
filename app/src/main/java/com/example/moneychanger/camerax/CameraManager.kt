package com.example.moneychanger.camerax

import com.example.moneychanger.mlkitOCR.TextRecognitionProcessor

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.ScaleGestureDetector
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.moneychanger.mlkitOCR.VisionType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay
) {

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var analyzerVisionType: VisionType = VisionType.OCR
    private var cameraSelectorOption = CameraSelector.LENS_FACING_BACK

    init {
        // Executor 생성 (스레드 풀)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun selectAnalyzer(): TextRecognitionProcessor {
        return TextRecognitionProcessor(
            graphicOverlay, // GraphicOverlay 전달
            onTextDetected = { detectedText ->
                // 텍스트가 감지되었을 때 처리할 내용
                Log.d("DetectedText", detectedText) // 로그에 감지된 텍스트 출력
            }
        )
    }

    // 카메라 설정
    private fun setCameraConfig(cameraProvider: ProcessCameraProvider?, cameraSelector: CameraSelector) {
        try {
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            preview?.setSurfaceProvider(finderView.surfaceProvider)  // 화면 표시
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    // 카메라 실행
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            Runnable {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 최신 프레임만 사용
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, selectAnalyzer())  // 텍스트 분석기 설정
                    }

                // 카메라 설정
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .build()

                val imageCapture = ImageCapture.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()

                setCameraConfig(cameraProvider, cameraSelector)  // 카메라 구성

            }, ContextCompat.getMainExecutor(context)
        )
    }

    companion object {
        private const val TAG = "CameraManager"
    }
}