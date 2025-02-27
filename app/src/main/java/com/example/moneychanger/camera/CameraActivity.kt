package com.example.moneychanger.camera

import android.content.ContentValues
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.moneychanger.databinding.ActivityCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

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

        // 강제
//        binding.previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE

//        startCamera()
        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        captureButton.setOnClickListener{
            takePicture()
        }

        setCallback(CallBackType.ON_SUCCESS) {recognizedText ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateInterval){
                val filteredText = extractItemsAndPrices(recognizedText)
                runOnUiThread{
                    binding.cameraText.text = filteredText
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

            // 기존 바인딩 해제
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

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
//                cameraProvider.unbindAll()
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
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
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
        private const val CAMERA_PERMISSION_CODE: Int = 10

    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // OCR에서 상품명과 가격만 필터링
    private fun extractItemsAndPrices(ocrText: String): String {
        val itemList = mutableListOf<String>()
        val lines = ocrText.split("\n")

        for (line in lines) {
            val match = Regex("(.+?)\\s+(\\d{1,3}(?:,\\d{3})*)$").find(line)
            val excludeKeywords = listOf("서울특별시", "합계", "부가세", "거스름돈", "판매일") // 필요 없는 항목

            if (match != null) {
                val (item, price) = match.destructured
                itemList.add("$item : $price 원")
            }
        }

        return if (itemList.isNotEmpty()) {
            itemList.joinToString("\n")
        } else {
            "상품을 인식할 수 없습니다."
        }
    }

}