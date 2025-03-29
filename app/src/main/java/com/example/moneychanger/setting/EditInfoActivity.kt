package com.example.moneychanger.setting

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityEditInfoBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.UpdateUserInfoRequest
import com.example.moneychanger.network.user.UserInfoResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditInfoBinding
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바 타이틀 숨기기

        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        // 기존 유저 정보 표시
        loadUserInfo()

        // 생년월일 선택
        binding.dateText.setOnClickListener { showDatePickerDialog() }

        // 수정하기 버튼 클릭 이벤트
        binding.buttonEdit.setOnClickListener { updateUserInfo() }
    }

    // ✅ 유저 정보 표시
    private fun loadUserInfo() {
        val userInfo = TokenManager.getUserInfo()

        if (userInfo != null) {
            val birthDate = userInfo.userDateOfBirth ?: ""
            binding.dateText.setText(birthDate)
            binding.editUserName.setText(userInfo.userName ?: "")
            binding.textUserEmail.text = userInfo.userEmail ?: "이메일 없음"

            Log.d("EditInfoActivity", "✅ 불러온 생년월일: $birthDate")
        } else {
            Toast.makeText(this, "로그인 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ 날짜 선택 다이얼로그
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.dateText.setText(selectedDate)
                Log.d("EditInfoActivity", "✅ 선택된 생년월일: $selectedDate")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ✅ 회원정보 업데이트
    private fun updateUserInfo() {
        val newUserName = binding.editUserName.text.toString().trim()
        val newUserBirth = binding.dateText.text.toString().trim()
        val userInfo = TokenManager.getUserInfo()
        val userEmail = userInfo?.userEmail ?: ""

        if (newUserName.isEmpty()) {
            Toast.makeText(this, "이름과 생년월일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val finalUserBirth = if (newUserBirth.isEmpty()) {
            userInfo?.userDateOfBirth ?: ""
        } else {
            newUserBirth
        }

        val updateRequest = UpdateUserInfoRequest(
            userEmail = userEmail,
            userDateOfBirth = finalUserBirth,
            userName = newUserName,
            userPassword = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = TokenManager.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditInfoActivity, "로그인 토큰이 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.updateUserInfo("Bearer $accessToken", updateRequest)
                val apiResponse = response.body()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && apiResponse?.status == "success") {
                        val data = apiResponse.data
                        if (data != null) {
                            val jsonData = Gson().toJson(data)
                            val updatedUserInfo = Gson().fromJson(jsonData, UserInfoResponse::class.java)
                            TokenManager.saveUserInfo(updatedUserInfo)
                            Toast.makeText(this@EditInfoActivity, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this@EditInfoActivity, SettingActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                        } else {
                            Toast.makeText(this@EditInfoActivity, "수정 성공했지만 사용자 정보가 반환되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            this@EditInfoActivity,
                            "수정 실패: ${apiResponse?.message ?: "서버 응답 없음"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("EditInfoActivity", "❌ 오류 발생: ${e.message}")
                    Toast.makeText(this@EditInfoActivity, "수정 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
