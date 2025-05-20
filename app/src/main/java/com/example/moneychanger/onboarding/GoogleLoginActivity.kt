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
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
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
                val idToken = account?.idToken
                val name = account?.displayName
                val email = account?.email
                val photoUrl = account?.photoUrl?.toString()
                Log.d("GoogleLogin", "이름: $name, 이메일: $email, 사진: $photoUrl")

                val serverAuthCode = account?.serverAuthCode  // 필요하면 accessToken 요청에 사용
                if (idToken != null && name != null) {
                    sendTokenToServer(idToken, name)
                } else {
                    Toast.makeText(this, "ID Token 없음", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "Google sign in failed", e)
                Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendTokenToServer(idToken: String, name: String) {
        val body = mapOf(
            "idToken" to idToken,
            "name" to name
        )
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
                            Toast.makeText(this@GoogleLoginActivity, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@GoogleLoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this@GoogleLoginActivity, "서버 응답 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GoogleLoginActivity, "통신 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}