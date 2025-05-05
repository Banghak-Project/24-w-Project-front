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
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.camera.core.AspectRatio
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.R
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.etc.ExchangeRateUtil.calculateExchangeRate
import com.example.moneychanger.etc.OnProductAddedListener
import com.example.moneychanger.etc.SlideCameraList
import com.example.moneychanger.location.LocationUtil
import com.example.moneychanger.location.getAddressFromLatLng
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.list.CreateListRequestDto
import com.example.moneychanger.network.list.CreateListResponseDto
import com.example.moneychanger.network.product.CreateProductRequestDto
import com.example.moneychanger.network.product.CreateProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.moneychanger.network.currency.CurrencyViewModel
import com.example.moneychanger.network.list.UpdateRequestDto
import com.example.moneychanger.network.list.UpdateResponseDto
import com.example.moneychanger.network.product.ProductResponseDto
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.checkerframework.checker.index.qual.GTENegativeOne

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CameraActivity : AppCompatActivity(), OnProductAddedListener {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CurrencyViewModel
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FrameLayout
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val selectedTexts = mutableListOf<String>() // 사용자가 선택한 텍스트 저장

    private var selectedProductName: String? = null
    private var selectedProductPrice: String? = null
    private var selectedProductNameView: View? = null
    private var selectedProductPriceView: View? = null
    private var isSelectingPrice = false // 현재 상품 가격 선택 중인지 확인하는 플래그

    private var currencyIdFrom = -1L
    private var currencyIdTo = -1L
    private val userId = TokenManager.getUserId()
    private lateinit var location : String

    private var latestListId = -1L
    private var saveedList: CreateListResponseDto? = null

    private var productList: MutableList<ProductResponseDto> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        previewView = binding.previewView
        captureButton = binding.cameraButton

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        binding.listButton.setOnClickListener {
            val currencyFromUnit = CurrencyManager.getById(currencyIdFrom)?.curUnit ?: ""
            val currencyToUnit = CurrencyManager.getById(currencyIdTo)?.curUnit ?: ""

            val slideCameraList = SlideCameraList.newInstance(
                productList,
                currencyIdFrom,
                currencyIdTo,
                currencyFromUnit,
                currencyToUnit
            )
            slideCameraList.show(supportFragmentManager, SlideCameraList.TAG)
        }


        captureButton.setOnClickListener {
            takePicture()
            binding.defaultText.visibility = GONE
            binding.newText.visibility = VISIBLE
            binding.offButton.visibility = VISIBLE
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        // 통화 정보 가져오기
        val currencyList = CurrencyManager.getCurrencies()

        if (userId == -1L) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 통화 Spinner 데이터 설정
        val currencyUnits: List<String> = currencyList.map { it.curUnit }
        val customSpinner1 = CustomSpinner(this, currencyUnits)
        val customSpinner2 = CustomSpinner(this, currencyUnits)

        // 바꿀 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                viewModel.updateCurrency(selected)
                val selectedCurrency = CurrencyManager.getByUnit(selected)
                currencyIdFrom = selectedCurrency.currencyId
                if (latestListId != -1L) {
                    updateListCurrency(currencyIdFrom, currencyIdTo)
                }
            }
        }

        // 바뀐 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                viewModel.updateCurrency(selected)
                val selectedCurrency = CurrencyManager.getByUnit(selected)
                currencyIdTo = selectedCurrency.currencyId
                if (latestListId != -1L) {
                    updateListCurrency(currencyIdFrom, currencyIdTo)
                }
            }
        }

        if (latestListId != -1L) {
            fetchProductsAndShowDialog(latestListId)
        } else {
            Toast.makeText(this, "리스트를 먼저 생성해주세요.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 화면 비율을 16:9로 설정
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 사진 촬영 비율을 16:9로 설정
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

                    // 비트맵 불러오면서 EXIF 회전 적용
                    val bitmap = loadBitmapWithRotation(savedUri)

                    runOnUiThread {
                        binding.capturedImageView.setImageBitmap(bitmap)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되었을 때 다시 위치 요청
                getLocation { address ->
                    Log.d("Debug", "✔ 권한 승인 후 위치: $address")
                }
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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

            binding.textOverlay.visibility = VISIBLE
            // 선택 완료 버튼 클릭 시, 새로운 리스트 생성 및 상품 추가
            binding.confirmButton.setOnClickListener {
                Log.d("Debug", "confirm 버튼 클릭 됨")
                val productNameCopy = selectedProductName
                val productPriceCopy = selectedProductPrice
                if (productNameCopy != null && productPriceCopy != null) {
                    if (latestListId != -1L) {
                        // 이미 리스트가 있다면 상품만 추가
                        addProductToList(latestListId, productNameCopy, productPriceCopy)
                    } else {
                        // 처음이라면 리스트 먼저 생성
                        checkAndRequestLocationPermission {
                            getLocation { address ->
                                Log.d("Debug", "새 리스트 생성중")
                                location = address
                                addNewList(userId, currencyIdFrom, currencyIdTo, location, productNameCopy, productPriceCopy)
                            }
                        }
                    }
                    // 이미지 뷰 → 카메라 프리뷰로 전환
                    binding.textOverlay.removeAllViews()
                    binding.capturedImageView.visibility = GONE
                    binding.previewView.visibility = VISIBLE

                    selectedProductName = null
                    selectedProductPrice = null
                    selectedProductNameView = null
                    selectedProductPriceView = null
                    selectedTexts.clear()
                    isSelectingPrice = false

                    binding.defaultText.visibility = VISIBLE
                    binding.newText.visibility = GONE
                    binding.offButton.visibility = GONE
                } else {
                    Toast.makeText(this@CameraActivity, "상품명과 상품 가격을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            Toast.makeText(this@CameraActivity, "상품명을 선택해주세요.", Toast.LENGTH_SHORT).show()

            // 사진 찍기 전으로 돌아가기
            binding.offButton.setOnClickListener{
                binding.textOverlay.removeAllViews()
                binding.capturedImageView.visibility = GONE
                binding.previewView.visibility = VISIBLE

                selectedProductName = null
                selectedProductPrice = null
                selectedProductNameView = null
                selectedProductPriceView = null
                selectedTexts.clear()
                isSelectingPrice = false

                binding.defaultText.visibility = VISIBLE
                binding.newText.visibility = GONE
                binding.offButton.visibility = GONE
            }
        }
    }
    private fun getLocation(onLocationReady: (String) -> Unit) {
        Log.d("Debug", "위치 함수 실행 시작")
        LocationUtil.getCurrentLocation(
            context = this,
            onSuccess = { location ->
                Log.d("Debug", "위치 함수 실행 성공")
                val lat = location.latitude
                val lng = location.longitude

                lifecycleScope.launch {
                    val address = getAddressFromLatLng(this@CameraActivity, lat, lng)
                    val addressInfo = address ?: "주소를 찾을 수 없습니다."
                    onLocationReady(addressInfo)
                }
            },
            onError = {
                Log.d("Debug", "위치 함수 실행 실패")
                onLocationReady("위치 정보를 가져올 수 없습니다.")
            }
        )
    }

    private val LOCATION_PERMISSION_CODE = 1001

    private fun checkAndRequestLocationPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            onGranted()
        }
    }


    private fun toggleSelection(view: View, text: String) {
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
            updateSelectedText()
        }
    }

    private fun cleanPriceText(priceText: String): String {
        val cleaned = priceText.replace(Regex("[^0-9.]"), "") // 숫자와 소수점만 남김
        val price = if (cleaned.matches(Regex("\\d+(\\.\\d+)?"))) cleaned else ""
        return price
    }

    private fun updateSelectedText() {
        if (selectedProductName != null && selectedProductPrice != null) {
            val cleanPrice = cleanPriceText(selectedProductPrice!!).toDouble()
            CoroutineScope(Dispatchers.Main).launch {
                val exchangedAmount = calculateExchangeRate(currencyIdFrom, currencyIdTo, cleanPrice)
                binding.productName.text = selectedProductName
                binding.productOriginPrice.text = cleanPrice.toString()
                binding.productCalcPrice.text = exchangedAmount.toString()
            }
        }
    }

    private fun addNewList(userId: Long, currencyIdFrom: Long, currencyIdTo: Long, location: String, productNameCopy: String, productPriceCopy: String) {
        val createRequest = CreateListRequestDto(userId, currencyIdFrom, currencyIdTo, location)
        Log.d("CameraActivity", "🚀 리스트 생성 요청 데이터: userId=$userId, currencyIdFrom=$currencyIdFrom, currencyIdTo=$currencyIdTo, location=$location")

        // 리스트 추가 API 호출 (비동기 방식)
        apiService.createList(createRequest)
            .enqueue(object : Callback<ApiResponse<CreateListResponseDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<CreateListResponseDto>>,
                    response: Response<ApiResponse<CreateListResponseDto>>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == "success") {
                            val jsonData = Gson().toJson(apiResponse.data)
                            val createListResponse: CreateListResponseDto? = try {
                                Gson().fromJson(jsonData, CreateListResponseDto::class.java)
                            } catch (e: JsonSyntaxException) {
                                Log.e("CameraActivity", "🚨 JSON 변환 오류: ${e.message}")
                                null
                            }

                            if (createListResponse != null) {
                                val listId = createListResponse.listId ?: -1L
                                if (listId != -1L) {
                                    Toast.makeText(this@CameraActivity, "리스트 추가 완료!", Toast.LENGTH_SHORT).show()
                                    Log.d("CameraActivity", "✅ 리스트 생성 성공: ID=$listId")

                                    latestListId = listId
                                    saveedList = createListResponse

                                    fetchProductsAndShowDialog(listId)

                                    addProductToList(listId, productNameCopy, productPriceCopy)
                                } else {
                                    Log.e("CameraActivity", "🚨 리스트 ID 오류 발생")
                                }
                            } else {
                                Log.e("CameraActivity", "🚨 리스트 응답 데이터 변환 실패")
                            }
                        } else {
                            Log.e("CameraActivity", "🚨 리스트 추가 실패: ${apiResponse?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        Log.e("CameraActivity", "🚨 응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CreateListResponseDto>>, t: Throwable) {
                    Log.e("CameraActivity", "🚨 서버 요청 실패: ${t.message}")
                }
            })
    }

    private fun addProductToList(listId: Long, productName: String, price: String) {
        val cleanPrice = cleanPriceText(price!!).toDouble()
        val productRequest = CreateProductRequestDto(listId, productName, cleanPrice)

        apiService.createProduct(productRequest)
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
                                Toast.makeText(this@CameraActivity, "상품 추가 완료!", Toast.LENGTH_SHORT).show()
                                Log.d("CameraActivity", "✅ 상품 추가 성공: ${productResponse.name}")

                                val resultIntent = Intent()
                                setResult(RESULT_OK, resultIntent)
                                fetchProductsAndShowDialog(listId)
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

    private fun fetchProductsAndShowDialog(listId: Long) {
        apiService.getProductByListsId(listId)
            .enqueue(object : Callback<ApiResponse<List<ProductResponseDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<ProductResponseDto>>>,
                    response: Response<ApiResponse<List<ProductResponseDto>>>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == "success") {
                            val productListDto = apiResponse.data ?: emptyList()
                            productList = productListDto.toMutableList()

                        } else {
                            Toast.makeText(this@CameraActivity, "상품 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("CameraActivity", "🚨 상품 목록 응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<ProductResponseDto>>>, t: Throwable) {
                    Log.e("CameraActivity", "🚨 상품 목록 서버 요청 실패: ${t.message}")
                }
            })
    }

    private fun updateListCurrency(currencyFromId: Long, currencyToId: Long) {
        val updateRequest = UpdateRequestDto(
            listId = saveedList!!.listId,
            currencyIdFrom = currencyFromId,
            currencyIdTo = currencyToId,
            location = saveedList!!.location,
            name = saveedList!!.name
        )

        apiService.updateList(updateRequest)
            .enqueue(object : Callback<ApiResponse<UpdateResponseDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<UpdateResponseDto>>,
                    response: Response<ApiResponse<UpdateResponseDto>>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Log.i("ListActivity", "✅ 서버에 리스트 업데이트 완료")
                        setResult(RESULT_OK)
                    } else {
                        Log.e("ListActivity", "❌ 서버 응답 실패: ${response.errorBody()?.string()}")
                        Toast.makeText(this@CameraActivity, "리스트 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UpdateResponseDto>>, t: Throwable) {
                    Log.e("ListActivity", "❌ 서버 업데이트 실패", t)
                    Toast.makeText(this@CameraActivity, "서버 통신 오류", Toast.LENGTH_SHORT).show()
                }
            })
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