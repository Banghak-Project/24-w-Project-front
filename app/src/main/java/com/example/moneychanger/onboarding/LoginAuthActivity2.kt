package com.example.moneychanger.onboarding

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityLoginAuth2Binding
import com.example.moneychanger.network.user.OtpRequest
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.user.EmailRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class LoginAuthActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAuth2Binding
    private lateinit var countDownTimer: CountDownTimer
    private var isInputReceived = false // 입력 여부 확인 변수
    private lateinit var email: String // 이메일은 무조건 있어야 하므로 lateinit으로 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAuth2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 숨기기

        // 뒤로 가기 버튼 동작
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        // 이메일 값 받기
        val receivedEmail = intent.getStringExtra("email")
        if (receivedEmail.isNullOrEmpty()) {
            Log.e("LoginAuthActivity2", "이메일 값이 전달되지 않았습니다!")
            Toast.makeText(this, "이메일 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            finish()
            return
        } else {
            email = receivedEmail
            Log.d("LoginAuthActivity2", "받은 이메일: $email")
        }

        // 이메일 표시
        binding.inputEmail.text = email

        binding.buttonNext.setOnClickListener {
            val otp = binding.inputField.text.toString().trim()
            Log.d("LoginAuthActivity2", "전송할 이메일: $email, OTP: $otp")
            if (otp.isEmpty()) {
                Toast.makeText(this, "인증 코드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(email!!, otp)
        }

        startCountDown(3 * 60 * 1000)

        // 새 코드 보내기 버튼 스타일링 및 동작
        binding.buttonNewcode.paintFlags =
            binding.buttonNewcode.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.buttonNewcode.setOnClickListener {
            sendNewCode()
        }

        // 입력 필드에 텍스트 변화 감지
        binding.inputField.setOnKeyListener { _, _, _ ->
            isInputReceived = true // 입력이 감지되면 true로 변경
            false
        }

        // 카운트다운 타이머 시작
        startCountDown(3 * 60 * 1000)
    }

    // 카운트다운 타이머 구현
    private fun startCountDown(startTimeInMillis: Long) {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel() // 기존 타이머 중지
        }

        countDownTimer = object : CountDownTimer(startTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60

                // "mm:ss" 형식으로 텍스트 업데이트
                binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.timerText.text = "00:00"
                if (!isInputReceived) {
                    // 입력이 없을 경우 토스트 메시지 표시
                    Toast.makeText(
                        this@LoginAuthActivity2,
                        "인증 시간이 만료되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        countDownTimer.start()
        isInputReceived = false // 새 타이머 시작 시 입력 상태 초기화
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel() // 타이머 해제
        }
    }

    private fun sendNewCode() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.sendOtp(EmailRequest(email))
                val responseBody = response.errorBody()?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@LoginAuthActivity2,
                            "새 인증 코드가 전송되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        startCountDown(3 * 60 * 1000) // 타이머 재시작
                    } else {
                        Log.e("LoginAuthActivity2", "응답 오류: $responseBody")
                        Toast.makeText(
                            this@LoginAuthActivity2,
                            "새 코드 요청 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginAuthActivity2", "네트워크 오류: ${e.message}")
                    Toast.makeText(this@LoginAuthActivity2, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun verifyOtp(email: String, otp: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.verifyOtp(OtpRequest(email, otp))

                // 🚨 서버 응답 확인
                val responseBody = response.body()?.string() ?: ""
                Log.d("LoginAuthActivity2", "서버 응답 본문: $responseBody")

                // JSON 파싱
                val jsonResponse = JSONObject(responseBody)
                val message = jsonResponse.optString("message", "")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && message == "이메일 인증 성공") {
                        val intent = Intent(this@LoginAuthActivity2, PersonalInfoActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginAuthActivity2, "인증 실패: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginAuthActivity2", "네트워크 오류 발생: ${e.message}")
                    Toast.makeText(this@LoginAuthActivity2, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}