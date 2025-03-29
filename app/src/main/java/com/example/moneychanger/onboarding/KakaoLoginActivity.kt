package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.network.ApiService
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.KakaoLoginRequest
import com.example.moneychanger.network.user.SignInResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class KakaoLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_select)

        // 카카오 로그인 버튼 참조
        val kakaoLoginButton = findViewById<LinearLayout>(R.id.button_kakao_login)

        // 카카오 로그인 버튼 클릭 이벤트
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
    }

    // ✅ 카카오 로그인 실행
    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 📌 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        } else {
            // 📌 카카오 계정으로 로그인 (카카오톡이 없을 경우)
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        }
    }

    // ✅ 카카오 로그인 처리
    private fun handleKakaoLogin(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            android.util.Log.d("KakaoLoginActivity", "✅ 카카오 로그인 성공! 토큰: ${token.accessToken}")

            // ✅ accessToken을 서버로 전달
            sendTokenToServer(token.accessToken)
        }
    }

    private fun sendTokenToServer(accessToken: String) {
        Log.d("KakaoLoginActivity", "🚀 서버로 카카오 로그인 요청 중... 토큰: $accessToken")

        val apiService = RetrofitClient.apiService
        val request = KakaoLoginRequest(accessToken)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.kakaoSignIn(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        if (responseBody?.status == "success") {
                            val responseData = responseBody.data

                            if (responseData != null) {
                                Log.d("KakaoLoginActivity", "✅ 서버 응답: 로그인 성공")

                                // ✅ 토큰 저장
                                responseData.accessToken?.let { TokenManager.saveAccessToken(it) }
                                responseData.refreshToken?.let { TokenManager.saveRefreshToken(it) }

                                Toast.makeText(this@KakaoLoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@KakaoLoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Log.e("KakaoLoginActivity", "🚨 로그인 실패: data 필드가 null")
                                Toast.makeText(this@KakaoLoginActivity, "로그인 실패: 서버 응답 없음", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("KakaoLoginActivity", "🚨 로그인 실패: ${responseBody?.message}")
                            Toast.makeText(this@KakaoLoginActivity, "로그인 실패: ${responseBody?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("KakaoLoginActivity", "🚨 서버 오류: ${response.errorBody()?.string()}")
                        Toast.makeText(this@KakaoLoginActivity, "서버 오류: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("KakaoLoginActivity", "🚨 서버 오류: ${e.message}")
                    Toast.makeText(this@KakaoLoginActivity, "서버 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}