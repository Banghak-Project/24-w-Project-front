package com.example.moneychanger.onboarding

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityPersonalInfoBinding
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.currency.CurrencyResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.network.user.SignUpRequest
import com.example.moneychanger.network.user.SignUpResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private lateinit var email: String // 이메일 필수
    private lateinit var otp: String // OTP 필수
    private lateinit var agreedTerms: List<Boolean> // 이용약관 동의 내역
    private var selectedGender: Boolean? = null // true: 남성, false: 여성
    private var selectedDateOfBirth: String = "" // "yyyy-MM-dd" 포맷
    private var defaultCurrencyId: Long = 14L
    private val currencyDisplayList = mutableListOf<String>()
    private val currencyIdMap = mutableMapOf<String, Long>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바 숨기기

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        // 동의 내역 받기 (ArrayList<Boolean>로 받기)
        agreedTerms = intent.getSerializableExtra("agreedTerms") as? ArrayList<Boolean> ?: arrayListOf(false, false, false)
        Log.d("PersonalInfoActivity", "✅ 받은 동의 데이터: $agreedTerms")


        // 이메일 값 받기 (LoginAuthActivity에서 전달됨)
        email = intent.getStringExtra("email")?.lowercase(Locale.getDefault()) ?: "" // ✅ 이메일 소문자로 변환
        if (email.isEmpty()) {
            Log.e("PersonalInfo", "이메일 값이 없습니다!")
            Toast.makeText(this, "이메일 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.inputEmail.text = email

        // OTP 값 받기 (LoginAuthActivity2에서 전달됨)
        otp = intent.getStringExtra("otp") ?: ""
        if (otp.isNullOrEmpty()) {
            Log.e("PersonalInfoActivity", "OTP 값이 전달되지 않았습니다!")
            Toast.makeText(this, "OTP 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("PersonalInfoActivity", "전달받은 OTP: $otp")  // 디버깅 로그 추가
        }


        // 성별 버튼 선택 이벤트
        binding.buttonMale.setOnClickListener {
            selectedGender = true
            updateGenderSelectionUI()
            Log.d("PersonalInfoActivity", "Male selected")
        }
        binding.buttonFemale.setOnClickListener {
            selectedGender = false
            updateGenderSelectionUI()
            Log.d("PersonalInfoActivity", "Female selected")
        }

        // 날짜 선택 기능
        setupDatePicker()

        loadCurrencyOptions()

        // 다음 버튼 클릭 시 회원가입 요청
        binding.buttonNext.setOnClickListener {
            sendSignUpRequest()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, null, year, month, day)
        datePickerDialog.datePicker.init(year, month, day) { _, selectedYear, selectedMonth, selectedDay ->
            selectedDateOfBirth = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            binding.dateText.setText(selectedDateOfBirth)
            Log.d("PersonalInfoActivity", "선택한 생년월일: $selectedDateOfBirth")
        }

        binding.dateText.setOnClickListener {
            datePickerDialog.show()
        }
    }

    private fun loadCurrencyOptions() {
        RetrofitClient.apiService.findAll().enqueue(object :
            Callback<ApiResponse<List<CurrencyResponseDto>>> {
            override fun onResponse(
                call: Call<com.example.moneychanger.network.user.ApiResponse<List<com.example.moneychanger.network.currency.CurrencyResponseDto>>>,
                response: Response<ApiResponse<List<CurrencyResponseDto>>>
            ) {
                if (response.isSuccessful) {
                    val dtoList = response.body()?.data ?: emptyList()
                    val modelList = dtoList.map { dto ->
                        CurrencyModel(
                            currencyId = dto.currencyId,
                            curUnit = dto.curUnit,
                            dealBasR = dto.dealBasR.toDoubleOrNull() ?: 0.0,
                            curNm = dto.curNm
                        )
                    }
                    CurrencyManager.setCurrencies(modelList)
                    currencyDisplayList.clear()
                    currencyIdMap.clear()
                    modelList.forEach {
                        val display = it.toString()
                        currencyDisplayList.add(display)
                        currencyIdMap[display] = it.currencyId
                    }
                    binding.inputCurrency.setOnClickListener {
                        AlertDialog.Builder(this@PersonalInfoActivity)
                            .setTitle("기본 통화 선택")
                            .setItems(currencyDisplayList.toTypedArray()) { _, index ->
                                val selected = currencyDisplayList[index]
                                binding.inputCurrency.hint = selected
                                defaultCurrencyId = currencyIdMap[selected] ?: 14L
                            }.show()
                    }
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<List<CurrencyResponseDto>>>,
                t: Throwable
            ) {
                Toast.makeText(this@PersonalInfoActivity, "통화 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    //     회원가입 요청
    private fun sendSignUpRequest() {
        val name = binding.inputName.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        if (name.isEmpty() || password.isEmpty() || selectedGender == null || selectedDateOfBirth.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(selectedDateOfBirth)
        val dateOfBirthMillis = date?.time ?: System.currentTimeMillis()


        val signUpRequest = SignUpRequest(
            userName = name,
            userDateOfBirth = dateOfBirthMillis,
            userGender = selectedGender ?: false,
            userEmail = email, // ✅ 소문자로 변환된 이메일 사용
            userPassword = password,
            otp = otp,
            agreedTerms = agreedTerms,
            defaultCurrencyId = defaultCurrencyId
        )

        Log.d("PersonalInfoActivity", "📩 보낼 회원가입 요청 데이터: $signUpRequest")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.signUp(signUpRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val signUpResponse = response.body()
                        Log.d("PersonalInfoActivity", "✅ 회원가입 응답 데이터: ${Gson().toJson(signUpResponse)}")

                        var signUpData: SignUpResponse? = null

                        if (signUpResponse?.message == "회원가입 성공") {
                            val userName = signUpResponse?.data?.userName ;
                            Toast.makeText(this@PersonalInfoActivity, "${signUpResponse.message} ($userName 님)", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@PersonalInfoActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@PersonalInfoActivity, signUpResponse?.message ?: "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("PersonalInfoActivity", "🚨 회원가입 실패 - HTTP ${response.code()}: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PersonalInfoActivity", "⚠️ 예외 발생: ${e.message}")
                }
            }
        }
    }

    private fun updateGenderSelectionUI() {
        binding.buttonMale.isSelected = selectedGender == true
        binding.buttonFemale.isSelected = selectedGender == false
    }
}
