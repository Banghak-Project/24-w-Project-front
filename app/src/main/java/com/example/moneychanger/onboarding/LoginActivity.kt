package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityLoginBinding
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.SignInRequest
import com.example.moneychanger.network.user.SignInResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

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
                val normalizedEmail = email.trim().lowercase(Locale.getDefault())
                val signInRequest = SignInRequest(userEmail = normalizedEmail, userPassword = password)

                Log.d("LoginActivity", "🚀 로그인 요청 데이터: $signInRequest")

                val response = RetrofitClient.apiService.signIn(signInRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        Log.d("LoginActivity", "✅ 원본 서버 응답: $apiResponse")

                        if (apiResponse != null && apiResponse.status == "success" && apiResponse.data != null) {
                            try {
                                // `data`를 `SignInResponse`로 변환
                                val signInResponse: SignInResponse = Gson().fromJson(
                                    Gson().toJson(apiResponse.data), SignInResponse::class.java
                                )

                                Log.d("LoginActivity", "✅ 파싱된 SignInResponse: $signInResponse")

                                if (signInResponse.msg == "로그인 성공") {
                                    val accessToken = signInResponse.accessToken ?: ""

                                    if (accessToken.isNotEmpty()) {
                                        TokenManager.saveAccessToken(accessToken)

                                        // ✅ UI 업데이트 보장
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()

                                            // ✅ MainActivity로 이동
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                    } else {
                                        Toast.makeText(this@LoginActivity, "로그인 실패: 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@LoginActivity, signInResponse.msg ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: JsonSyntaxException) {
                                Log.e("LoginActivity", "🚨 JSON 변환 오류: ${e.message}")
                                Toast.makeText(this@LoginActivity, "서버 응답 오류 (데이터 변환 실패)", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, apiResponse?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginActivity", "🚨 로그인 실패 - HTTP ${response.code()}: $errorBody")
                        Toast.makeText(this@LoginActivity, "로그인 실패: 서버 오류 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "🚨 HTTP 오류: ${e.message}")
                    Toast.makeText(this@LoginActivity, "로그인 실패: 서버 오류", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "🌐 네트워크 오류: ${e.message}")
                    Toast.makeText(this@LoginActivity, "로그인 실패: 네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "⚠️ 예외 발생: ${e.message}")
                    Toast.makeText(this@LoginActivity, "로그인 실패: 알 수 없는 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
