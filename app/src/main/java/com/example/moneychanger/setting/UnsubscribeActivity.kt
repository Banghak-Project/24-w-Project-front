package com.example.moneychanger.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityUnsubscribeBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnsubscribeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        // '회원탈퇴가 완료되었습니다' 화면으로 이동
        // 탈퇴 기능 추기
        binding.buttonUnsubscribe.setOnClickListener {
            val inputPassword = binding.passwordEditText.text.toString()
            val token = TokenManager.getAccessToken()

            if (inputPassword.isBlank()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.withdrawal(
                        "Bearer $token",
                        mapOf("password" to inputPassword)
                    )

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            TokenManager.clearTokens()
                            startActivity(
                                Intent(
                                    this@UnsubscribeActivity,
                                    UnsubscribeSuccessActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            Toast.makeText(
                                this@UnsubscribeActivity,
                                response.body()?.message ?: "탈퇴 실패", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@UnsubscribeActivity,
                            "네트워크 오류: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}