package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.databinding.ActivityLoginSelectBinding

class LoginSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonKakaoLogin.setOnClickListener{
            // 카카오톡 로그인 페이지로 연결
        }

        binding.buttonEmailLogin.setOnClickListener{
            // 이메일 로그인 페이지로 연결
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}