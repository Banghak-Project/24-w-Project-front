package com.example.moneychanger.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 스플래시 화면 표시 후 메인 액티비티로 전환
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 스플래시 액티비티를 종료
        }, 3000) // 3000ms = 3초
    }
}