package com.example.moneychanger.setting

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityEditInfoBinding
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.user.UpdateUserInfoRequest
import com.example.moneychanger.network.user.UserInfoResponse
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.network.currency.CurrencyResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditInfoBinding
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // — 기본통화용 프로퍼티들 —
    private val currencyDisplayList = mutableListOf<String>()
    private val currencyIdMap       = mutableMapOf<String, Long>()
    private var defaultCurrencyId   = 14L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        // 1) 기존 유저 정보 표시 (이 안에서 defaultCurrencyId도 셋팅)
        loadUserInfo()

        // 2) 통화 목록 불러오고 다이얼로그로 선택
        loadCurrencyOptions()

        // 3) 다이얼로그 띄우기
        binding.inputCurrency.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("기본 통화 선택")
                .setItems(currencyDisplayList.toTypedArray()) { _, which ->
                    val display = currencyDisplayList[which]
                    binding.inputCurrency.hint = display
                    defaultCurrencyId = currencyIdMap[display] ?: defaultCurrencyId
                }
                .show()
        }

        // 생년월일 선택
        binding.dateText.setOnClickListener { showDatePickerDialog() }

        // 수정하기 버튼 클릭
        binding.buttonEdit.setOnClickListener { updateUserInfo() }
    }

    private fun loadUserInfo() {
        val userInfo = TokenManager.getUserInfo()
        if (userInfo != null) {
            binding.editUserName.setText(userInfo.userName ?: "")
            binding.textUserEmail.text = userInfo.userEmail ?: ""
            binding.dateText.setText(userInfo.userDateOfBirth ?: "")

            // 기존 기본통화 아이디
            defaultCurrencyId = userInfo.defaultCurrencyId

            Log.d("EditInfoActivity", "불러온 기본통화ID=$defaultCurrencyId")
        } else {
            Toast.makeText(this, "로그인 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val date = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.dateText.setText(date)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadCurrencyOptions() {
        RetrofitClient.apiService
            .findAll()  // 모든 통화 조회 엔드포인트
            .enqueue(object : Callback<ApiResponse<List<CurrencyResponseDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<CurrencyResponseDto>>>,
                    response: Response<ApiResponse<List<CurrencyResponseDto>>>
                ) {
                    if (response.isSuccessful) {
                        val dtos = response.body()?.data ?: emptyList()
                        val models = dtos.map {
                            CurrencyModel(
                                currencyId = it.currencyId,
                                curUnit    = it.curUnit,
                                dealBasR   = it.dealBasR.toDoubleOrNull() ?: 0.0,
                                curNm      = it.curNm
                            )
                        }
                        CurrencyManager.setCurrencies(models)

                        currencyDisplayList.clear()
                        currencyIdMap.clear()
                        models.forEach { m ->
                            val display = m.toString() // ex) "₩ 한국 원"
                            currencyDisplayList += display
                            currencyIdMap[display] = m.currencyId
                        }

                        // 유저의 기존 기본통화를 hint로 보여주기
                        val existing = models.find { it.currencyId == defaultCurrencyId }
                        existing?.let {
                            val key = it.toString()
                            binding.inputCurrency.hint = key
                        }
                    } else {
                        Toast.makeText(this@EditInfoActivity, "통화 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<CurrencyResponseDto>>>, t: Throwable) {
                    Toast.makeText(this@EditInfoActivity, "통화 정보 로드 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateUserInfo() {
        val newName  = binding.editUserName.text.toString().trim()
        val newBirth = binding.dateText.text.toString().trim()
        val userInfo = TokenManager.getUserInfo()
        val email    = userInfo?.userEmail ?: ""

        if (newName.isEmpty() || newBirth.isEmpty()) {
            Toast.makeText(this, "이름과 생년월일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateUserInfoRequest(
            userEmail        = email,
            userDateOfBirth  = newBirth,
            userName         = newName,
            userPassword     = null,
            defaultCurrencyId = defaultCurrencyId   // ← 추가된 필드
        )

        CoroutineScope(Dispatchers.IO).launch {
            val token = TokenManager.getAccessToken().orEmpty()
            try {
                val resp = RetrofitClient.apiService
                    .updateUserInfo("Bearer $token", request)
                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful && resp.body()?.status == "success") {
                        resp.body()?.data?.let { data ->
                            // 서버에서 최신 UserInfoResponse를 받고 저장
                            val json = Gson().toJson(data)
                            val updated = Gson().fromJson(json, UserInfoResponse::class.java)
                            TokenManager.saveUserInfo(updated)
                            Toast.makeText(this@EditInfoActivity, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(
                                Intent(this@EditInfoActivity, SettingActivity::class.java)
                                    .apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                            )
                        } ?: run {
                            Toast.makeText(this@EditInfoActivity, "수정됐으나 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@EditInfoActivity, "수정 실패: ${resp.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("EditInfoActivity", "❌ 오류: ${e.message}")
                    Toast.makeText(this@EditInfoActivity, "오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

