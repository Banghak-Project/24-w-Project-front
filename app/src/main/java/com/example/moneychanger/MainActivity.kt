package com.example.moneychanger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import com.example.moneychanger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 리스트 버튼 클릭 이벤트 설정
        binding.buttonList.setOnClickListener{
            // 리스트 모아보기
        }

        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener{
            // 카메라 api와 연결하여 동작할 내용
        }

        // 임시 버튼 연결
        binding.b1.setOnClickListener{
            // 로그인 선택 페이지로 연결
            val intent = Intent(this, LoginSelectActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }
}