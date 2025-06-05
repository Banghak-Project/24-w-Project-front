// app/src/main/java/com/example/moneychanger/onboarding/find/IdResultActivity.kt
package com.example.moneychanger.onboarding.find

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.adapter.EmailAdapter
import com.example.moneychanger.databinding.ActivityIdResultBinding
import com.example.moneychanger.onboarding.LoginActivity

class IdResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단 툴바 세팅
        val toolbar: Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로 가기 버튼
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        val userName = intent.getStringExtra("userName") ?: "사용자"
        val emailList: ArrayList<String> =
            intent.getStringArrayListExtra("emailList") ?: arrayListOf()

        binding.textViewUserName.text = userName

        // RecyclerView 에 어댑터 연결
        val adapter = EmailAdapter(emailList)
        val recycler = findViewById<RecyclerView>(R.id.list_container)
        recycler.adapter = adapter

        // “로그인 화면으로” 버튼 클릭 시 LoginActivity 로 이동
        binding.buttonToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
