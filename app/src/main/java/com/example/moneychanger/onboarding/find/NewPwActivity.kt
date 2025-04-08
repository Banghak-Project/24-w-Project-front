package com.example.moneychanger.onboarding.find

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityNewPwBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.ResetPasswordRequest
import com.example.moneychanger.onboarding.LoginActivity
import kotlinx.coroutines.launch

class NewPwActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPwBinding
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("userEmail") ?: ""

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
        // 새 비밀번호 설정 완료 버튼 클릭 이벤트 처리
        binding.buttonToLogin.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val currentPassword = binding.pwOrigin.text.toString().trim()
        val newPassword = binding.pwNew.text.toString().trim()
        val confirmPassword = binding.pwAgain.text.toString().trim()

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val userEmail = intent.getStringExtra("userEmail") ?: run {
            Toast.makeText(this, "이메일 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val requestBody = mapOf(
                    "userEmail" to userEmail,
                    "currentPassword" to currentPassword,
                    "newPassword" to newPassword,
                    "confirmPassword" to confirmPassword
                )

                val response = RetrofitClient.apiService.resetPassword(requestBody)

                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@NewPwActivity, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    val errorMsg = response.body()?.message ?: "비밀번호 변경 실패"
                    Toast.makeText(this@NewPwActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NewPwActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}