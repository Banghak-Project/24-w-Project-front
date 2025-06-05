package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityLoginSelectBinding
import com.example.moneychanger.home.NaviContainerActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.network.user.KakaoLoginRequest
import com.example.moneychanger.network.user.SignInResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSelectBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TokenManager.init(applicationContext)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(getString(R.string.default_web_client_id), true)
            .build()

        Log.d("GoogleLogin", "client_id = ${getString(R.string.default_web_client_id)}")

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        // 구글 로그인 버튼 클릭 이벤트 추가
        binding.buttonGoogleLogin.setOnClickListener{
            val googleLoginButton = findViewById<LinearLayout>(R.id.button_google_login)
            googleLoginButton.setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
            }
        }


        // 카카오 로그인 버튼 클릭 이벤트 추가
        binding.buttonKakaoLogin.setOnClickListener{
            val kakaoLoginButton = findViewById<LinearLayout>(R.id.button_kakao_login)
            kakaoLoginButton.setOnClickListener {
                kakaoLogin()
            }
        }

        // 이메일 로그인 버튼 클릭 이벤트
        binding.buttonEmailLogin.setOnClickListener{
            // 이메일 로그인 페이지로 연결
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // 이제 idToken 대신 serverAuthCode 를 꺼냅니다.
                val authCode = account?.serverAuthCode
                Log.d("GoogleLogin", "authCode: $authCode")

                if (authCode != null) {
                    sendAuthCodeToServer(authCode)
                } else {
                    Toast.makeText(this, "서버 인증 코드가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "Google sign in failed", e)
                Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendAuthCodeToServer(authCode: String) {
        // 백엔드가 기대하는 키는 "authCode"
        val body = mapOf("authCode" to authCode)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.googleSignIn(body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val result: SignInResponse? = response.body()?.data
                        result?.let {
                            TokenManager.saveAccessToken(it.accessToken ?: "")
                            TokenManager.saveRefreshToken(it.refreshToken ?: "")
                            TokenManager.saveSignInInfo(it)
                            TokenManager.saveUserId(it.userId)
                            Toast.makeText(
                                this@LoginSelectActivity,
                                "구글 로그인 성공",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (it.firstSocialLogin == true && it.socialProvider == "google") {
                                Toast.makeText(
                                    this@LoginSelectActivity,
                                    "소셜 계정 최초 로그인입니다. 설정 메뉴에서 기본 통화를 지정해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            startActivity(
                                Intent(
                                    this@LoginSelectActivity,
                                    NaviContainerActivity::class.java
                                )
                            )
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginSelectActivity,
                            "서버 응답 오류: ${response.body()?.message ?: response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginSelectActivity,
                        "통신 오류: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
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

                            Toast.makeText(this@LoginSelectActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            if (data.firstSocialLogin == true && data.socialProvider == "kakao") {
                                Toast.makeText(
                                    this@LoginSelectActivity,
                                    "소셜 계정 최초 로그인입니다. 설정 메뉴에서 기본 통화를 지정해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            startActivity(Intent(this@LoginSelectActivity, NaviContainerActivity::class.java))
                            finish()
                        } else {
                            Log.e("KakaoLoginActivity", "🚨 로그인 실패: data가 null")
                            Toast.makeText(this@LoginSelectActivity, "로그인 실패: 서버 응답 없음", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("KakaoLoginActivity", "🚨 서버 오류: $errorMessage")
                        Toast.makeText(this@LoginSelectActivity, "서버 오류: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("KakaoLoginActivity", "🚨 예외 발생: ${e.message}", e)
                    Toast.makeText(this@LoginSelectActivity, "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}