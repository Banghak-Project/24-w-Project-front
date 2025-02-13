package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityLoginBinding
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.SignInResponse
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.SignInRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바 타이틀 숨김

        // 뒤로 가기 버튼 설정
        binding.buttonSignIn.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signIn(email, password)
        }

        // 아이디/비밀번호 찾기
        binding.buttonFindIdPw.paintFlags = binding.buttonFindIdPw.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        binding.buttonFindIdPw.setOnClickListener {
            // 아이디 비밀번호 찾기 로직 추가 가능
        }

        // 회원가입 이동
        binding.buttonSignUp.paintFlags = binding.buttonSignUp.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this, PolicyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: SignInResponse = RetrofitClient.apiService.signIn(SignInRequest(email, password))
                runOnUiThread {
                    if (response.msg == "로그인 성공") {
                        // 토큰 저장
                        TokenManager.saveAccessToken(response.accessToken ?: "")

                        // 홈 화면 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, response.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "로그인 실패: 서버 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
