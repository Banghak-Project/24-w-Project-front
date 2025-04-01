package com.example.moneychanger.list

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityAddBinding
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
import com.example.moneychanger.network.currency.CurrencyViewModel

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

        binding.inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputText = s.toString().replace(",", "")  // 쉼표 제거
                val amount = inputText.toDoubleOrNull() ?: 0.0

                if (amount > 0) {
                    val convertedAmount = calculateExchangeRate(currencyIdFrom,currencyIdTo,amount)
                    binding.changedText.text = String.format(Locale.US, "%,.2f", convertedAmount)
                } else {
                    binding.changedText.text = "0.00"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.buttonAdd.setOnClickListener {
            val inputText = binding.inputField.text.toString().replace(",", "")
            val amount = inputText.toDoubleOrNull() ?: 0.0

            if (amount > 0) {
                val convertedAmount = calculateExchangeRate(currencyIdFrom, currencyIdTo, amount)
                addProductToList(listId, "", amount)
            }
        }
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
            Log.e("ExchangeRate", "🚨 환율 값이 유효하지 않습니다: rateFrom=$rateFrom, rateTo=$rateTo")
            return 0.0
        }

        // 👇 (100) 단위를 가진 통화는 보정값 설정
        val fromDivisor = if (fromCurrency.curUnit.contains("(100)") == true) 100.0 else 1.0
        val toDivisor = if (toCurrency.curUnit.contains("(100)") == true) 100.0 else 1.0

        val adjustedRateFrom = rateFrom / fromDivisor
        val adjustedRateTo = rateTo / toDivisor

        val exchangedAmount = (amount * adjustedRateFrom) / adjustedRateTo

        Log.d("ExchangeRate", "✅ ${fromCurrency.curUnit} -> ${toCurrency.curUnit} 환율 적용: $amount -> $exchangedAmount")
        return exchangedAmount
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
                                Log.e("CameraActivity", "🚨 JSON 변환 오류: ${e.message}")
                                null
                            }

                            if (productResponse != null) {
                                Toast.makeText(this@AddActivity, "상품 추가 완료!", Toast.LENGTH_SHORT).show()
                                Log.d("CameraActivity", "✅ 상품 추가 성공: ${productResponse.name}")

                                // 리스트로 돌아가서 업데이트하도록 결과 전달
                                val resultIntent = Intent()
                                setResult(RESULT_OK, resultIntent)
                                finish()
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
}