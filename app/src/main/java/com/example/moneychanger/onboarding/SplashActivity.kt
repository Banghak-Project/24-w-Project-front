package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.home.NaviContainerActivity
import com.example.moneychanger.network.TokenManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        TokenManager.init(this) // SharedPreferences 초기화
        validateTokenOnStart()

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (TokenManager.getAccessToken().isNullOrBlank()) {
                Intent(this, LoginSelectActivity::class.java) // 로그인 안 된 상태
            } else {
                Intent(this, NaviContainerActivity::class.java) // 로그인 유지
            }
            startActivity(nextActivity)
            finish()
        }, 3000) // 3초 후 전환
    }

    private fun validateTokenOnStart() {
        val token = TokenManager.getAccessToken()
        if (!token.isNullOrBlank()) {
            try {
                val parts = token.split(".")
                if (parts.size != 3) {
                    TokenManager.clearTokens()
                    return
                }

                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                val regex = Regex("\"exp\":(\\d+)")
                val matchResult = regex.find(payload)
                val exp = matchResult?.groupValues?.get(1)?.toLong() ?: 0L

                val currentTime = System.currentTimeMillis() / 1000 // 현재 시간 (초 단위)

                if (exp < currentTime) {
                    // 🔥 만료됨: 토큰 제거
                    TokenManager.clearTokens()
                }

            } catch (e: Exception) {
                // 🔥 토큰 파싱 오류 시 토큰 제거
                TokenManager.clearTokens()
            }
        }
    }
}
