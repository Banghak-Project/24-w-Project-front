package com.example.moneychanger.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityLoginAuthBinding
import com.example.moneychanger.network.user.EmailRequest
import com.example.moneychanger.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


class LoginAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // 다음 버튼 클릭 리스너
        binding.buttonNext.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // otp 전송 요청
            sendOtp(email)
        }
    }

    // 이메일 인증 otp 전송
    private fun sendOtp(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<Void> = RetrofitClient.apiService.sendOtp(EmailRequest(email))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@LoginAuthActivity, "인증 코드가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginAuthActivity, LoginAuthActivity2::class.java)
                        intent.putExtra("email", email)
                        Log.d("LoginAuthActivity", "전달할 이메일: $email")
                        startActivity(intent)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        // 실제로 보여줄 메시지
                        val errorMessage = try {
                            // 서버가 보낸 JSON에서 "message" 필드만 파싱
                            JSONObject(errorBody).optString("message", "OTP 전송 실패")
                        } catch (e: JSONException) {
                            // JSON 파싱조차 안 될 때는 raw body 또는 기본 메시지
                            errorBody ?: "OTP 전송 실패"
                        }
                        Log.e("LoginAuthActivity", "OTP 전송 실패: $errorBody")
                        Toast.makeText(this@LoginAuthActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginAuthActivity", "HTTP 오류: ${e.message}")
                    Toast.makeText(this@LoginAuthActivity, "서버 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginAuthActivity", "네트워크 오류: ${e.message}")
                    Toast.makeText(this@LoginAuthActivity, "네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginAuthActivity", "예외 발생: ${e.message}")
                    Toast.makeText(this@LoginAuthActivity, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}