package com.example.moneychanger.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityUnsubscribeSuccessBinding
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.home.NaviContainerActivity

class UnsubscribeSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribeSuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribeSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // 처음으로 (메인)
        binding.buttonToStart.setOnClickListener{
            val intent = Intent(this, NaviContainerActivity::class.java)
            startActivity(intent)
        }
    }
}