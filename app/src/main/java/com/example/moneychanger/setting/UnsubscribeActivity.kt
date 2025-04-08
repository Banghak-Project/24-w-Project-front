package com.example.moneychanger.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityUnsubscribeBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
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
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        binding.buttonUnsubscribe.setOnClickListener {
            performWithdrawal()
        }
    }

    private fun performWithdrawal() {
        val inputPassword = binding.passwordEditText.text.toString().trim()

        if (inputPassword.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val requestBody = mapOf("password" to inputPassword)
                val response = RetrofitClient.apiService.withdrawal(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        TokenManager.clearTokens()
                        Toast.makeText(this@UnsubscribeActivity, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        navigateToUnsubscribeSuccess()
                    } else {
                        Log.e("UnsubscribeActivity", "⚠️ 탈퇴 실패: ${response.body()}")
                        Toast.makeText(
                            this@UnsubscribeActivity,
                            response.body()?.message ?: "회원탈퇴 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UnsubscribeActivity", "⚠️ 네트워크 예외 발생: ${e.message}")
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

    private fun navigateToUnsubscribeSuccess() {
        val intent = Intent(this, UnsubscribeSuccessActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}
