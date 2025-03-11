package com.example.moneychanger.onboarding.find

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityFindIdPwBinding
import com.example.moneychanger.databinding.ActivityIdResultBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.onboarding.LoginActivity

class IdResultActivity : BaseActivity() {
    private lateinit var binding: ActivityIdResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        val userName = intent.getStringExtra("userName") ?: "사용자"
        val userEmail = intent.getStringExtra("userEmail") ?: "아이디를 찾을 수 없습니다."
        binding.textViewUserName.text = userName
        val resultTextView: TextView = findViewById(R.id.textViewIdResult)
        resultTextView.text = userEmail ?: "아이디를 찾을 수 없습니다."


        // 로그인 화면으로
        binding.buttonToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}