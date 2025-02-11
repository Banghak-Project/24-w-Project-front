package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.RetrofitClient.KakaoLoginRequest
import com.example.moneychanger.home.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KakaoLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_select)

        // 카카오 SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))

        // 카카오 로그인 버튼 참조 (activity_login_select.xml에서는 LinearLayout)
        val kakaoLoginButton = findViewById<LinearLayout>(R.id.button_kakao_login)

        // 카카오 로그인 버튼 클릭 이벤트
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
    }

    // 카카오 로그인 처리
    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡으로 로그인 시도
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        } else {
            // 카카오 계정으로 로그인 시도 (카카오톡 미설치 시)
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        }
    }

    // 로그인 결과 처리
    private fun handleKakaoLogin(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            // 로그인 성공 → 백엔드로 액세스 토큰 전달
            sendTokenToServer(token.accessToken)
        }
    }

    // 백엔드 서버로 카카오 액세스 토큰 전달
    private fun sendTokenToServer(accessToken: String) {
        val apiService = RetrofitClient.apiService
        val request = KakaoLoginRequest(accessToken) // RetrofitClient 내부에 있는 KakaoLoginRequest 사용

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.kakaoSignIn(request)
                withContext(Dispatchers.Main) {
                    if (response.msg == "카카오 로그인 성공") {
                        Toast.makeText(this@KakaoLoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@KakaoLoginActivity, MainActivity::class.java))
                        finish() // 현재 액티비티 종료
                    } else {
                        Toast.makeText(this@KakaoLoginActivity, response.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@KakaoLoginActivity, "서버 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
