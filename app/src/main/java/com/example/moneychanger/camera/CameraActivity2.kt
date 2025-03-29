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
import android.widget.ImageView
import androidx.camera.core.AspectRatio
import androidx.lifecycle.ViewModelProvider
import com.example.moneychanger.R
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.etc.DataProvider
import com.example.moneychanger.etc.OnProductAddedListener
import com.example.moneychanger.etc.SlideCameraList
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.product.CreateProductRequestDto
import com.example.moneychanger.network.product.CreateProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CameraActivity2 : AppCompatActivity(), OnProductAddedListener {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CurrencyViewModel
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FrameLayout
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val selectedTexts = mutableListOf<String>()

    private var selectedProductName: String? = null
    private var selectedProductPrice: String? = null
    private var selectedProductNameView: View? = null
    private var selectedProductPriceView: View? = null
    private var isSelectingPrice = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        previewView = binding.previewView
        captureButton = binding.cameraButton

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 리스트에서 가져온 정보들
        var currencyIdFrom = intent.getLongExtra("currencyIdFrom", -1L)
        var currencyIdTo = intent.getLongExtra("currencyIdTo", -1L)
        val listId = intent.getLongExtra("listId", -1L)

        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        binding.listButton.setOnClickListener {
            val productList = DataProvider.productDummyModel
            val slideCameraList = SlideCameraList.newInstance(productList)
            slideCameraList.show(supportFragmentManager, SlideCameraList.TAG)
        }

        captureButton.setOnClickListener {
            takePicture(currencyIdFrom,currencyIdTo,listId)
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        val currencyList = CurrencyManager.getCurrencies()
        val currencyUnits: List<String> = currencyList.map { it.curUnit } ?: emptyList()
        val customSpinner1 = CustomSpinner(this, currencyUnits)
        val customSpinner2 = CustomSpinner(this, currencyUnits)

        val currencyFrom = CurrencyManager.getById(currencyIdFrom)
        val currencyTo = CurrencyManager.getById(currencyIdTo)

        if (currencyFrom == null || currencyTo == null) {
            Log.e("MainActivity", "⚠️ 통화 정보 매핑 실패:")
        }else{
            // 리스트에서 전달받은 통화 ID를 기반으로 초기값 설정
            val initialCurrencyFrom = currencyFrom.curUnit
            val initialCurrencyTo = currencyTo.curUnit

            if (initialCurrencyFrom.isNotEmpty()) {
                binding.currencyName1.text = initialCurrencyFrom
                viewModel.updateCurrency(initialCurrencyFrom)
            }

            if (initialCurrencyTo.isNotEmpty()) {
                binding.currencyName2.text = initialCurrencyTo
                viewModel.updateCurrency(initialCurrencyTo)
            }
        }


        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                viewModel.updateCurrency(selected)
                val selectedCurrency = CurrencyManager.getByUnit(selected)
                currencyIdFrom = selectedCurrency.currencyId
            }
        }

        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                viewModel.updateCurrency(selected)
                val selectedCurrency = CurrencyManager.getByUnit(selected)
                currencyIdTo = selectedCurrency.currencyId
            }
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
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

    private fun takePicture(currencyIdFrom : Long, currencyIdTo : Long, listId: Long) {
        if (currencyIdFrom == -1L || currencyIdTo == -1L) {
            Toast.makeText(this, "두 통화를 모두 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

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

                    val bitmap = loadBitmapWithRotation(savedUri)

                    runOnUiThread {
                        binding.capturedImageView.setImageBitmap(bitmap)
                        binding.previewView.visibility = View.INVISIBLE
                        binding.capturedImageView.visibility = View.VISIBLE
                    }

                    recognizeTextFromBitmap(bitmap, currencyIdFrom, currencyIdTo, listId)
                }
            }
        )
    }

    private fun loadBitmapWithRotation(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

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

    private fun recognizeTextFromBitmap(bitmap: Bitmap, currencyIdFrom : Long, currencyIdTo : Long,listId: Long) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                displayRecognizedText(visionText, bitmap, currencyIdFrom, currencyIdTo, listId)
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "텍스트 인식 실패: ${e.localizedMessage}")
                Toast.makeText(this, "텍스트 인식 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayRecognizedText(visionText: Text, bitmap: Bitmap, currencyIdFrom : Long, currencyIdTo : Long, listId: Long) {
        binding.textOverlay.removeAllViews()
        selectedTexts.clear()

        binding.capturedImageView.post {
            val displayedWidth = binding.capturedImageView.width.toFloat()
            val displayedHeight = binding.capturedImageView.height.toFloat()

            val originalWidth = bitmap.width.toFloat()
            val originalHeight = bitmap.height.toFloat()

            val scaleX = displayedWidth / originalWidth
            val scaleY = displayedHeight / originalHeight

            val offsetX = (displayedWidth - (originalWidth * scaleX)) / 2
            val offsetY = (displayedHeight - (originalHeight * scaleY)) / 2

            Log.d("OCR", "🔍 Scale Factor: X=$scaleX, Y=$scaleY, OffsetX: $offsetX, OffsetY: $offsetY")

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val rect = line.boundingBox ?: continue
                    val angle = line.angle

                    val boxPadding = 4
                    val adjustedWidth = (rect.width() * scaleX + boxPadding).toInt()
                    val adjustedHeight = (rect.height() * scaleY + boxPadding).toInt()

                    val borderView = View(this@CameraActivity2).apply {
                        setBackgroundResource(R.drawable.ocr_border)
                        isClickable = true
                        rotation = angle
                        setOnClickListener { toggleSelection(this, line.text, currencyIdFrom, currencyIdTo) }
                    }

                    val layoutParams = FrameLayout.LayoutParams(adjustedWidth, adjustedHeight).apply {
                        leftMargin = (rect.left * scaleX + offsetX - boxPadding / 2).toInt()
                        topMargin = (rect.top * scaleY + offsetY - boxPadding / 2).toInt()
                    }

                    binding.textOverlay.addView(borderView, layoutParams)
                }
            }

            binding.textOverlay.visibility = View.VISIBLE
            // 선택 완료 버튼 클릭 시, 새로운 상품 추가
            binding.confirmButton.setOnClickListener {
                if (selectedProductName != null && selectedProductPrice != null) {
                    addProductToList(listId , selectedProductName!!, selectedProductPrice!!)
                } else {
                    Toast.makeText(this@CameraActivity2, "상품명과 상품 가격을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            Toast.makeText(this@CameraActivity2, "상품명을 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSelection(view: View, text: String, currencyIdFrom : Long, currencyIdTo : Long) {
        if (selectedTexts.contains(text)) {
            selectedTexts.remove(text)
            view.setBackgroundResource(R.drawable.ocr_border)

            if (selectedProductName == text) {
                selectedProductName = null
                selectedProductNameView = null
                isSelectingPrice = false
                Toast.makeText(this, "상품명이 선택 해제되었습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else if (selectedProductPrice == text) {
                selectedProductPrice = null
                selectedProductPriceView = null
                isSelectingPrice = true
                Toast.makeText(this, "상품 가격이 선택 해제되었습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        selectedTexts.add(text)
        view.setBackgroundResource(R.drawable.ocr_border_selected)

        if (selectedProductName == null) {
            if (!text.any { it.isLetter() }) {
                Toast.makeText(this, "잘못된 선택입니다. 상품명을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            // 기존 선택한 뷰가 있으면 초기화
            selectedProductNameView?.setBackgroundResource(R.drawable.ocr_border)

            selectedProductName = text
            selectedProductNameView = view
            isSelectingPrice = true
            Toast.makeText(this, "상품 가격을 선택해주세요.", Toast.LENGTH_SHORT).show()

        } else {
            val cleanPrice = cleanPriceText(text)
            if (cleanPrice.isEmpty()) {
                Toast.makeText(this, "잘못된 선택입니다. 숫자로 된 가격을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            // 기존 가격 선택 뷰 초기화
            selectedProductPriceView?.setBackgroundResource(R.drawable.ocr_border)

            selectedProductPrice = text
            selectedProductPriceView = view
            updateSelectedText(currencyIdFrom, currencyIdTo)
        }
    }

    private fun cleanPriceText(priceText: String): String {
        val cleaned = priceText.replace(Regex("[^0-9.]"), "")
        val price = if (cleaned.matches(Regex("\\d+(\\.\\d+)?"))) cleaned else ""
        return price
    }

    private fun updateSelectedText(currencyIdFrom : Long, currencyIdTo : Long) {
        if (selectedProductName != null && selectedProductPrice != null) {
            val cleanPrice = cleanPriceText(selectedProductPrice!!).toDouble()
            val resultText = "상품명: ${selectedProductName}, 상품가격: ${cleanPrice} -> ${calculateExchangeRate(currencyIdFrom, currencyIdTo, cleanPrice)}"
            binding.cameraText.text = resultText
            Toast.makeText(this, "선택 완료: $resultText", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addProductToList(listId: Long, productName: String, price: String) {
        val cleanPrice = cleanPriceText(price!!).toDouble()
        val productRequest = CreateProductRequestDto(listId, productName, cleanPrice)

        RetrofitClient.apiService.createProduct(productRequest)
            .enqueue(object : Callback<ApiResponse<CreateProductResponseDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<CreateProductResponseDto>>,
                    response: Response<ApiResponse<CreateProductResponseDto>>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == "success") {
                            val jsonData = Gson().toJson(apiResponse.data)
                            val productResponse: CreateProductResponseDto? = try {
                                Gson().fromJson(jsonData, CreateProductResponseDto::class.java)
                            } catch (e: JsonSyntaxException) {
                                Log.e("CameraActivity", "🚨 JSON 변환 오류: ${e.message}")
                                null
                            }

                            if (productResponse != null) {
                                Toast.makeText(this@CameraActivity2, "상품 추가 완료!", Toast.LENGTH_SHORT).show()
                                Log.d("CameraActivity", "✅ 상품 추가 성공: ${productResponse.name}")

                                finish() // 상품 추가 후 액티비티 종료
                            } else {
                                Log.e("CameraActivity", "🚨 상품 응답 데이터 변환 실패")
                            }
                        } else {
                            Log.e("CameraActivity", "🚨 상품 추가 실패: ${apiResponse?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        Log.e("CameraActivity", "🚨 응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CreateProductResponseDto>>, t: Throwable) {
                    Log.e("CameraActivity", "🚨 서버 요청 실패: ${t.message}")
                }
            })
    }

    private fun calculateExchangeRate(fromId: Long, toId: Long, amount: Double): Double {
        val fromCurrency = CurrencyManager.getById(fromId)
        val toCurrency = CurrencyManager.getById(toId)
        if (fromCurrency == null || toCurrency == null) {
            Log.e("MainActivity", "⚠️ 통화 정보 매핑 실패:")
            return 0.0
        }
        val rateFrom = fromCurrency.dealBasR
        val rateTo = toCurrency.dealBasR

        if (rateFrom == 0.0 || rateTo == 0.0) {
            Log.e("ExchangeRate", "환율 값이 유효하지 않습니다: rateFrom=$rateFrom, rateTo=$rateTo")
            return 0.0
        }

        val fromDivisor = if (fromCurrency.curUnit?.contains("(100)") == true) 100.0 else 1.0
        val toDivisor = if (toCurrency.curUnit?.contains("(100)") == true) 100.0 else 1.0

        val adjustedRateFrom = rateFrom / fromDivisor
        val adjustedRateTo = rateTo / toDivisor

        val exchangedAmount = (amount * adjustedRateFrom) / adjustedRateTo

        Log.d("ExchangeRate", "✅ ${fromCurrency.curUnit} -> ${toCurrency.curUnit} 환율 적용: $amount -> $exchangedAmount")
        return exchangedAmount
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