package com.example.moneychanger.onboarding.find

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityFindIdPwBinding
import com.example.moneychanger.etc.BaseActivity

class FindIdPwActivity : BaseActivity() {
    private lateinit var binding: ActivityFindIdPwBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindIdPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        // 상단 버튼 선택 이벤트
        binding.btnFindId.setOnClickListener {
            Log.d("Debug", "아이디 버튼 클릭")
        }
        binding.btnResetPassword.setOnClickListener {
            Log.d("Debug", "비번 버튼 클릭")
        }
        binding.idText.setOnClickListener {
            binding.layoutFindID.visibility = View.VISIBLE
            binding.layoutFindPW.visibility = View.GONE

            binding.btnFindId.visibility = View.VISIBLE
            binding.idText.visibility = View.INVISIBLE
            binding.btnResetPassword.visibility = View.INVISIBLE
            binding.pwText.visibility = View.VISIBLE
            Log.d("Debug", "아이디 찾기 클릭")
        }
        binding.pwText.setOnClickListener {
            binding.layoutFindID.visibility = View.GONE
            binding.layoutFindPW.visibility = View.VISIBLE

            binding.btnFindId.visibility = View.INVISIBLE
            binding.idText.visibility = View.VISIBLE
            binding.btnResetPassword.visibility = View.VISIBLE
            binding.pwText.visibility = View.INVISIBLE
            Log.d("Debug", "비밀번호 재설정 클릭")
        }

        // 화면 전환 버튼 선택 이벤트
        binding.buttonToIdResult.setOnClickListener {
            val intent = Intent(this, IdResultActivity::class.java)
            startActivity(intent)
        }
        binding.buttonToPwResult.setOnClickListener {
            val intent = Intent(this, PwResultActivity::class.java)
            startActivity(intent)
        }
    }
}