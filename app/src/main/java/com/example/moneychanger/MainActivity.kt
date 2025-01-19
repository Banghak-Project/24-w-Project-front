package com.example.moneychanger

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.moneychanger.databinding.ActivityMainBinding
import com.example.moneychanger.mlkitOCR.TextRecognitionProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(){
    private lateinit var previewView: PreviewView // 카메라 미리보기 화면 표시
    private lateinit var cameraExecutor: ExecutorService // 카메라 작업을 비동기로 실행하기 위해 스레드 관리
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewView = binding.previewView
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }
    //카메라 미리보기 화면 초기화
    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            //미리보기 설정
            val preview = androidx.camera.core.Preview.Builder()
                .build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            //텍스트 분석 설정
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 최신 프레임만 유지
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, TextRecognitionProcessor(binding.graphicOverlay) { detectedText ->
                        runOnUiThread {
                            Log.w(TAG, detectedText)
                            binding.textView.text = detectedText // 인식된 텍스트를 텍스트 뷰에 표시
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // 후면카메라 사용

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()//카메라 작업 스레드 종료
    }
}