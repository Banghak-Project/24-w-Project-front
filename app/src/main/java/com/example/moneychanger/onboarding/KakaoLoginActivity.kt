package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.home.MainActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.KakaoLoginRequest
import com.example.moneychanger.network.user.SignInResponse
import com.example.moneychanger.network.user.ApiResponse
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class KakaoLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_select)

        // ✅ SharedPreferences 초기화
        TokenManager.init(applicationContext)

        // ✅ 카카오 로그인 버튼 클릭
        val kakaoLoginButton = findViewById<LinearLayout>(R.id.button_kakao_login)
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
    }

    // ✅ 카카오 로그인 실행
    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        }
    }

    // ✅ 로그인 성공/실패 후 처리
    private fun handleKakaoLogin(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            Log.d("KakaoLoginActivity", "✅ 카카오 로그인 성공! 토큰: ${token.accessToken}")
            sendTokenToServer(token.accessToken)
        }
    }

    // ✅ 서버로 카카오 accessToken 전송
    private fun sendTokenToServer(accessToken: String) {
        Log.d("KakaoLoginActivity", "🚀 서버로 카카오 로그인 요청 중... 토큰: $accessToken")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<ApiResponse<SignInResponse>> =
                    RetrofitClient.apiService.kakaoSignIn(KakaoLoginRequest(accessToken))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val data = responseBody?.data

                        if (data != null) {
                            Log.d("KakaoLoginActivity", "✅ 서버 응답: 로그인 성공")
                            Log.d("KakaoLoginActivity", "✅ 받은 accessToken: ${data.accessToken}")
                            Log.d("KakaoLoginActivity", "✅ 받은 refreshToken: ${data.refreshToken}")

                            TokenManager.saveAccessToken(data.accessToken ?: "")
                            TokenManager.saveRefreshToken(data.refreshToken ?: "")
                            TokenManager.saveUserId(data.userId)

                            Log.d("KakaoLoginActivity", "✅ 저장된 accessToken 확인용: ${TokenManager.getAccessToken()}")

                            Toast.makeText(this@KakaoLoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this@KakaoLoginActivity,
                                "소셜 계정 최초 로그인입니다. 설정 메뉴에서 기본 통화를 지정해주세요.",
                                Toast.LENGTH_LONG).show()

                            startActivity(Intent(this@KakaoLoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("KakaoLoginActivity", "🚨 로그인 실패: data가 null")
                            Toast.makeText(this@KakaoLoginActivity, "로그인 실패: 서버 응답 없음", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("KakaoLoginActivity", "🚨 서버 오류: $errorMessage")
                        Toast.makeText(this@KakaoLoginActivity, "서버 오류: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("KakaoLoginActivity", "🚨 예외 발생: ${e.message}", e)
                    Toast.makeText(this@KakaoLoginActivity, "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
