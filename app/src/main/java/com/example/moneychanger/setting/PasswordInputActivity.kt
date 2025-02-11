package com.example.moneychanger.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityPasswordInputBinding

class PasswordInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordInputBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // '회원탈퇴가 완료되었습니다' 화면으로 이동
        // 탈퇴 기능 추기
        binding.buttonUnsubscribe.setOnClickListener{
            val intent = Intent(this, UnsubscribeSuccessActivity::class.java)
            startActivity(intent)
        }
    }
}