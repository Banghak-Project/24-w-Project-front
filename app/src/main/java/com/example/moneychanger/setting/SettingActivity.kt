package com.example.moneychanger.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.moneychanger.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.pageText.text = "설정"
    }
}