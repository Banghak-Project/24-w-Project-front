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
            .requestIdToken(getString(R.string.default_web_client_id))  // OAuth용
            .build()

        // ⚠️ 반드시 클래스 필드에 할당
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleLoginButton = findViewById<LinearLayout>(R.id.button_google_login)
        googleLoginButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            Log.d("GoogleLogin", "🟢 구글 로그인 시작 - Intent 전송")
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("GoogleLogin", "✅ account 가져오기 성공")
                Log.d("GoogleLogin", "Account Email: ${account?.email}")
                Log.d("GoogleLogin", "Account DisplayName: ${account?.displayName}")
                Log.d("GoogleLogin", "Account IdToken: ${account?.idToken}")

                val idToken = account?.idToken
                if (idToken != null) {
                    Log.d("GoogleLogin", "🔐 ID Token 정상 수신")
                    sendTokenToServer(idToken)
                } else {
                    Log.e("GoogleLogin", "❌ ID Token이 null입니다. Web Client ID 설정 확인 필요")
                    Toast.makeText(this, "ID Token 없음 (로그 확인)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "❌ Google sign in 실패", e)
                Toast.makeText(this, "구글 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendTokenToServer(idToken: String) {
        Log.d("GoogleLogin", "📤 서버로 ID Token 전송 시작: $idToken")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.googleSignIn(mapOf("accessToken" to idToken))
                withContext(Dispatchers.Main) {
                    Log.d("GoogleLogin", "📥 서버 응답 수신: code=${response.code()}, 성공여부=${response.isSuccessful}")

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val result: SignInResponse? = response.body()?.data
                        result?.let {
                            Log.d("GoogleLogin", "✅ 로그인 성공: userId=${it.userId}, userName=${it.userName}")
                            TokenManager.saveAccessToken(it.accessToken ?: "")
                            TokenManager.saveRefreshToken(it.refreshToken ?: "")
                            TokenManager.saveSignInInfo(it)
                            TokenManager.saveUserId(it.userId)
                            Toast.makeText(this@GoogleLoginActivity, "구글 로그인 성공", Toast.LENGTH_SHORT).show()

                            // TODO: 메인 액티비티로 이동
                            startActivity(Intent(this@GoogleLoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.e("GoogleLogin", "❌ 서버 응답 실패: ${response.errorBody()?.string()}")
                        Toast.makeText(this@GoogleLoginActivity, "서버 응답 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GoogleLogin", "❌ 서버 요청 중 오류 발생", e)
                    Toast.makeText(this@GoogleLoginActivity, "통신 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
