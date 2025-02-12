package com.example.moneychanger.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityEditInfoBinding

class EditInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // 수정하기 버튼 클릭 이벤트
        binding.buttonEdit.setOnClickListener {
            // 설정 화면으로 돌아오기
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }
}