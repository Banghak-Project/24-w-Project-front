package com.example.moneychanger.onboarding.find

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityPwResultBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.onboarding.LoginActivity

class PwResultActivity : BaseActivity() {
    private lateinit var binding: ActivityPwResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPwResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }


        // 로그인 화면으로
        binding.buttonToNewPw.setOnClickListener {
            val intent = Intent(this, NewPwActivity::class.java)
            startActivity(intent)
        }
    }
}