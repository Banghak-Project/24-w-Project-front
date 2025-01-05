package com.example.moneychanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.moneychanger.databinding.ActivityLoginAuthBinding

class LoginAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : TextView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        binding.buttonNext.setOnClickListener {
            // 이메일 인증 페이지2로 연결
            val intent = Intent(this, LoginAuthActivity2::class.java)
            startActivity(intent)

            // 인증 메일 보내는 코드도 추가
        }
    }
}