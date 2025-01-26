package com.example.moneychanger.camera

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.moneychanger.databinding.ActivityCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var recognizedNum : String

    private var lastUpdateTime: Long = 0
    private val updateInterval = 2000 // 2초 간격

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewView = binding.previewView
        captureButton = binding.cameraButton

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        captureButton.setOnClickListener{
            takePicture()
        }

        setCallback(CallBackType.ON_SUCCESS) {recognizedText ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateInterval){
                recognizedNum = recognizedText.replace("[^0-9]".toRegex(), "")
                runOnUiThread{
                    binding.cameraText.text = "인식된 숫자: $recognizedNum"
            }
            lastUpdateTime = currentTime
            }
        }
        setCallback(CallBackType.ON_FAIL){ errorMessage ->
            runOnUiThread {
                binding.cameraText.text = errorMessage
            }
        }
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.surfaceProvider = previewView.surfaceProvider
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 지연 최소화 설정
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, getImageAnalyzer())
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try{
                Log.v("CameraActivity", "startCamera.try")
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)
            }catch (exc:Exception){
                Log.e("CameraActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // 콜백 타입 정의
    enum class CallBackType {
        ON_SUCCESS,
        ON_FAIL
    }

    // 콜백 함수 저장용 맵
    private val callBacks: MutableMap<CallBackType, (String) -> Unit> = mutableMapOf()

    // 콜백 함수 설정
    fun setCallback(type: CallBackType, callback: (String) -> Unit) {
        callBacks[type] = callback
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun getImageAnalyzer(): ImageAnalysis.Analyzer {
//        Log.d(TAG, "Average luminosity: $luma")
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return ImageAnalysis.Analyzer{ imageProxy ->
            val mediaImage = imageProxy.image
            mediaImage?.let{
                val image = InputImage.fromMediaImage(
                    mediaImage, imageProxy.imageInfo.rotationDegrees
                )
                recognizer.process(image)
                    .addOnSuccessListener{ text ->
                    if (text.text.isNotEmpty()) {
                        Log.d("TextAnalyzer", "텍스트 내용: ${text.text}")
                        callBacks[CallBackType.ON_SUCCESS]?.invoke(text.text)
                    }else{
                        Log.d("TextAnalyzer", "텍스트가 감지되지 않았습니다.")
                        callBacks[CallBackType.ON_FAIL]?.invoke("텍스트가 감지되지 않았습니다.")
                    }
                }
                    .addOnCompleteListener{
                        imageProxy.close()
                        mediaImage.close()
                    }
                    .addOnFailureListener {
                        Log.d("TextAnalyzer", "텍스트 분석 실패: ${it.localizedMessage}")
                        callBacks[CallBackType.ON_FAIL]?.invoke("텍스트 분석에 실패하였습니다.")
                    }
            }?:run{
                imageProxy.close() // 이미지가 null인 경우 리소스 해제
            }
        }
    }

    private fun takePicture() {
        Log.v("takePicture", "Capture button clicked!")
        val imageCapture = imageCapture ?: return

        Log.v("takePicture", "came here")
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply{
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        //파일과 메타데이터를 포함한 output option 객체 생성
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException){
                    Log.v(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.v(TAG, "Photo : ${output.savedUri}")
                    Toast.makeText(baseContext, "Photo : ${output.savedUri}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    }

}