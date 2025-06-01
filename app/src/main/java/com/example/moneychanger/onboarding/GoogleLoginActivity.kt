package com.example.moneychanger.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.home.NaviContainerActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.user.SignInResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleLoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_select)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(getString(R.string.default_web_client_id), true)
            .build()

        Log.d("GoogleLogin", "client_id = ${getString(R.string.default_web_client_id)}")

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleLoginButton = findViewById<LinearLayout>(R.id.button_google_login)
        googleLoginButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
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
                                this@GoogleLoginActivity,
                                "구글 로그인 성공",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (it.firstSocialLogin == true && it.socialProvider == "google") {
                                Toast.makeText(
                                    this@GoogleLoginActivity,
                                    "소셜 계정 최초 로그인입니다. 설정 메뉴에서 기본 통화를 지정해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            startActivity(
                                Intent(
                                    this@GoogleLoginActivity,
                                    NaviContainerActivity::class.java
                                )
                            )
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@GoogleLoginActivity,
                            "서버 응답 오류: ${response.body()?.message ?: response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@GoogleLoginActivity,
                        "통신 오류: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
