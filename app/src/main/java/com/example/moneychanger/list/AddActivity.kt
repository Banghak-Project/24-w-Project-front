package com.example.moneychanger.list

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityAddBinding
import com.example.moneychanger.etc.ExchangeRateUtil.calculateExchangeRate
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.product.CreateProductRequestDto
import com.example.moneychanger.network.product.CreateProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currencyIdFrom = intent.getLongExtra("currencyIdFrom", -1L)
        val currencyIdTo = intent.getLongExtra("currencyIdTo", -1L)
        val listId = intent.getLongExtra("listId", -1L)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        binding.loginToolbar.pageText.text = "추가하기"

        val currencyFrom = CurrencyManager.getById(currencyIdFrom)
        val currencyTo = CurrencyManager.getById(currencyIdTo)
        val fromUnit = currencyFrom?.curUnit ?: ""
        val toUnit = currencyTo?.curUnit ?: ""
        val fromKey = fromUnit.replace(Regex("\\(.*\\)"), "").trim()
        val toKey = toUnit.replace(Regex("\\(.*\\)"), "").trim()
        val fromResId = resources.getIdentifier(fromKey, "string", packageName)
        val toResId = resources.getIdentifier(toKey, "string", packageName)
        val fromSymbol = if (fromResId != 0) getString(fromResId) else fromKey
        val toSymbol = if (toResId != 0) getString(toResId) else toKey
        binding.currencyText1.text = fromUnit
        binding.currencyText2.text = toUnit
        binding.currencySymbol1.text = fromSymbol
        binding.currencySymbol2.text = toSymbol


        binding.inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputText = s.toString().replace(",", "")  // 쉼표 제거
                val amount = inputText.toDoubleOrNull() ?: 0.0

                if (amount > 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val convertedAmount = calculateExchangeRate(currencyIdFrom,currencyIdTo,amount)
                        binding.changedText.text = String.format(Locale.US, "%,.2f", convertedAmount)
                    }
                } else {
                    binding.changedText.text = "0.00"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        var pieces = 1
        binding.countText.text = pieces.toString()
        binding.buttonMinus.setOnClickListener {
            if (pieces > 1) {
                pieces -= 1
                binding.countText.text = pieces.toString()
            }

            if (pieces > 1) {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.main)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.main)
                )
            } else {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.gray_03)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.gray_03)
                )
            }
        }

        binding.buttonAdd.setOnClickListener {
            val inputText = binding.inputField.text.toString().replace(",", "")
            val inputName = binding.inputName.text.toString().trim()
            val amount = inputText.toDoubleOrNull() ?: 0.0

            if (amount > 0) {
                for (i in 0 until pieces) {
                    addProductToList(listId, inputName, amount)
                }
            }
        }

        binding.buttonPlus.setOnClickListener {
            pieces += 1
            binding.countText.text = pieces.toString()

            binding.rectMinus.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.main)
            )
            binding.minusSign.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.main)
            )
        }
    }

    private fun addProductToList(listId: Long, productName: String, price: Double) {
        val productRequest = CreateProductRequestDto(listId, productName, price)

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
                                Log.e("CameraActivity", "JSON 변환 오류: ${e.message}")
                                null
                            }

                            if (productResponse != null) {
                                Toast.makeText(this@AddActivity, "상품 추가 완료!", Toast.LENGTH_SHORT).show()
                                Log.d("CameraActivity", "상품 추가 성공: ${productResponse.name}")

                                // 리스트로 돌아가서 업데이트하도록 결과 전달
                                val resultIntent = Intent()
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            } else {
                                Log.e("CameraActivity", "상품 응답 데이터 변환 실패")
                            }
                        } else {
                            Log.e("CameraActivity", "상품 추가 실패: ${apiResponse?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        Log.e("CameraActivity", "응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CreateProductResponseDto>>, t: Throwable) {
                    Log.e("CameraActivity", "서버 요청 실패: ${t.message}")
                }
            })
    }
}