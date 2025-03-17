package com.example.moneychanger.camera

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.moneychanger.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import androidx.camera.core.AspectRatio
import com.example.moneychanger.R
import com.example.moneychanger.etc.DataProvider
import com.example.moneychanger.etc.OnProductAddedListener
import com.example.moneychanger.etc.SlideCameraList
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class CameraActivity : AppCompatActivity(), OnProductAddedListener {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FrameLayout
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val selectedTexts = mutableListOf<String>() // 사용자가 선택한 텍스트 저장

    private var selectedProductName: String? = null
    private var selectedProductPrice: String? = null
    private var isSelectingPrice = false // 현재 상품 가격 선택 중인지 확인하는 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewView = binding.previewView
        captureButton = binding.cameraButton

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        binding.listButton.setOnClickListener {
            val productList = DataProvider.productDummyModel  // 더미 데이터 가져오기
            val slideCameraList = SlideCameraList.newInstance(productList)  // newInstance() 사용
            slideCameraList.show(supportFragmentManager, SlideCameraList.TAG)
        }

        captureButton.setOnClickListener {
            takePicture()
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 📌 화면 비율을 16:9로 설정
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 📌 사진 촬영 비율을 16:9로 설정
                .build()


            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try{
                Log.v("CameraActivity", "startCamera.try")
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }catch (exc:Exception){
                Log.e("CameraActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraActivity", "사진 캡처 실패: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: return
                    Log.d("CameraActivity", "사진 저장됨: $savedUri")
//                    Toast.makeText(baseContext, "Photo : ${output.savedUri}", Toast.LENGTH_SHORT).show()

                    // 비트맵 불러오면서 EXIF 회전 적용
                    val bitmap = loadBitmapWithRotation(savedUri)

                    runOnUiThread {
                        binding.capturedImageView.setImageBitmap(bitmap) // 올바르게 회전된 이미지 표시
                        binding.previewView.visibility = View.INVISIBLE
                        binding.capturedImageView.visibility = View.VISIBLE
                    }

                    // OCR 실행
                    recognizeTextFromBitmap(bitmap)
                }
            }
        )
    }

    private fun loadBitmapWithRotation(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // ExifInterface를 사용해 원본 이미지의 회전 정보를 가져오기
        val exif = ExifInterface(contentResolver.openInputStream(uri)!!)
        val rotation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (rotation != 0) {
            rotateBitmap(bitmap, rotation)
        } else {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degree.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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

    private fun recognizeTextFromBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                displayRecognizedText(visionText, bitmap) // 비트맵 기준으로 OCR 박스 생성
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "텍스트 인식 실패: ${e.localizedMessage}")
                Toast.makeText(this, "텍스트 인식 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayRecognizedText(visionText: Text, bitmap: Bitmap) {
        binding.textOverlay.removeAllViews()
        selectedTexts.clear()

        binding.capturedImageView.post {
            val displayedWidth = binding.capturedImageView.width.toFloat()
            val displayedHeight = binding.capturedImageView.height.toFloat()

            val originalWidth = bitmap.width.toFloat()
            val originalHeight = bitmap.height.toFloat()

            // OCR 박스 크기 조정 (이미지의 실제 비율에 맞춰 보정)
            val scaleX = displayedWidth / originalWidth
            val scaleY = displayedHeight / originalHeight

            val offsetX = (displayedWidth - (originalWidth * scaleX)) / 2
            val offsetY = (displayedHeight - (originalHeight * scaleY)) / 2

            Log.d("OCR", "🔍 Scale Factor: X=$scaleX, Y=$scaleY, OffsetX: $offsetX, OffsetY: $offsetY")

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val rect = line.boundingBox ?: continue
                    val angle = line.angle  // ML Kit이 감지한 회전 각도 (기울어진 텍스트)

                    // 박스 크기 보정 (약간의 padding 추가)
                    val boxPadding = 4  // 4px 패딩 추가
                    val adjustedWidth = (rect.width() * scaleX + boxPadding).toInt()
                    val adjustedHeight = (rect.height() * scaleY + boxPadding).toInt()

                    val borderView = View(this@CameraActivity).apply {
                        setBackgroundResource(R.drawable.ocr_border)
                        isClickable = true
                        rotation = angle  // 기울어진 텍스트 각도를 OCR 박스에 적용
                        setOnClickListener { toggleSelection(this, line.text) }
                    }

                    val layoutParams = FrameLayout.LayoutParams(adjustedWidth, adjustedHeight).apply {
                        leftMargin = (rect.left * scaleX + offsetX - boxPadding / 2).toInt()
                        topMargin = (rect.top * scaleY + offsetY - boxPadding / 2).toInt()
                    }

                    binding.textOverlay.addView(borderView, layoutParams)
                }
            }

            binding.textOverlay.visibility = View.VISIBLE
            Toast.makeText(this@CameraActivity, "상품명을 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSelection(view: View, text: String) {
        if (selectedTexts.contains(text)) {
            // 이미 선택된 항목을 클릭하면 선택 해제
            selectedTexts.remove(text)
            view.setBackgroundResource(R.drawable.ocr_border) // 기본 테두리로 변경

            if (selectedProductName == text) {
                selectedProductName = null
                isSelectingPrice = false
                Toast.makeText(this, "상품명이 선택 해제되었습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else if (selectedProductPrice == text) {
                selectedProductPrice = null
                isSelectingPrice = true // 가격 선택 상태를 다시 활성화
                Toast.makeText(this, "상품 가격이 선택 해제되었습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // 선택되지 않은 항목을 클릭한 경우
        selectedTexts.add(text)
        view.setBackgroundResource(R.drawable.ocr_border_selected) // 선택된 상태

        if (selectedProductName == null) {
            // 상품명을 선택하는 단계 (숫자가 포함된 텍스트는 상품명이 아닐 확률이 높음)
            if (text.any { it.isDigit() }) {
                Toast.makeText(this, "잘못된 선택입니다. 상품명을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            selectedProductName = text
            isSelectingPrice = true
            Toast.makeText(this, "상품 가격을 선택해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            // 상품 가격을 선택하는 단계
            val cleanPrice = cleanPriceText(text)

            if (cleanPrice.isEmpty()) {
                Toast.makeText(this, "잘못된 선택입니다. 숫자로 된 가격을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            // 기존에 선택된 가격이 있으면 해제하고 새로운 가격 선택
            if (selectedProductPrice != null) {
                selectedTexts.remove(selectedProductPrice) // UI에서 선택 해제
            }

            selectedProductPrice = text
            updateSelectedText()
        }
    }

    private fun cleanPriceText(priceText: String): String {
        val cleaned = priceText.replace(Regex("[^0-9.]"), "") // 숫자와 소수점만 남김
        return if (cleaned.matches(Regex("\\d+(\\.\\d+)?"))) cleaned else ""
    }

    private fun updateSelectedText() {
        if (selectedProductName != null && selectedProductPrice != null) {
            val cleanPrice = cleanPriceText(selectedProductPrice!!)
            val resultText = "상품명: ${selectedProductName}, 상품가격: ${cleanPrice}"
            binding.cameraText.text = resultText
            Toast.makeText(this, "선택 완료: $resultText", Toast.LENGTH_SHORT).show()
        }
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

    override fun onProductAdded(productName: String, price: Double) {
        Log.d("CameraActivity", "상품명: $productName, 가격: $price")
    }

}