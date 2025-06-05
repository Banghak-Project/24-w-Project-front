package com.example.moneychanger.onboarding.find

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityFindIdPwBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.network.user.FindPasswordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FindIdPwActivity : BaseActivity() {
    private lateinit var binding: ActivityFindIdPwBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindIdPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        // 생년월일 선택
        binding.dateText.setOnClickListener { showDatePickerDialog() }

        // “아이디 찾기” 레이아웃/버튼 토글
        binding.btnFindId.setOnClickListener { /* 별도 로직 필요 없다면 생략 */ }
        binding.btnResetPassword.setOnClickListener { /* 별도 로직 필요 없다면 생략 */ }
        binding.idText.setOnClickListener {
            binding.layoutFindID.visibility = View.VISIBLE
            binding.layoutFindPW.visibility = View.GONE
            binding.btnFindId.visibility = View.VISIBLE
            binding.idText.visibility = View.INVISIBLE
            binding.btnResetPassword.visibility = View.INVISIBLE
            binding.pwText.visibility = View.VISIBLE
        }
        binding.pwText.setOnClickListener {
            binding.layoutFindID.visibility = View.GONE
            binding.layoutFindPW.visibility = View.VISIBLE
            binding.btnFindId.visibility = View.INVISIBLE
            binding.idText.visibility = View.VISIBLE
            binding.btnResetPassword.visibility = View.VISIBLE
            binding.pwText.visibility = View.INVISIBLE
        }

        // ID 찾기 요청
        binding.buttonToIdResult.setOnClickListener {
            val userName = binding.editTextName.text.toString().trim()
            val birthDate = binding.dateText.text.toString().trim()

            if (userName.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // ① API 호출: 이제 String → List<String> 반환
                    val response = RetrofitClient.apiService.findId(userName, birthDate)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.status == "success") {
                                // ② data: List<String> 으로 받아서 ArrayList 로 변환
                                val emailList: List<String> = apiResponse.data ?: emptyList()
                                if (emailList.isEmpty()) {
                                    Toast.makeText(
                                        this@FindIdPwActivity,
                                        "일치하는 사용자가 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // ③ IdResultActivity 로 이메일 목록을 넘긴다
                                    moveToIdResult(userName, ArrayList(emailList))
                                }
                            } else {
                                Toast.makeText(
                                    this@FindIdPwActivity,
                                    apiResponse?.message ?: "아이디 찾기 실패",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@FindIdPwActivity,
                                "서버 오류: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@FindIdPwActivity,
                            "네트워크 오류: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


        // ▶ 비밀번호 찾기 요청 (기존 로직 그대로)
        binding.buttonToPwResult.setOnClickListener {
            val userName = binding.editTextNamePw.text.toString().trim()
            val userEmail = binding.editTextEmailPw.text.toString().trim()

            if (userName.isEmpty() || userEmail.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val requestBody = FindPasswordRequest(userEmail, userName)
                    val response = RetrofitClient.apiService.findPassword(requestBody)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.status == "success") {
                                moveToPwResult(userEmail)
                            } else {
                                Toast.makeText(
                                    this@FindIdPwActivity,
                                    apiResponse?.message ?: "비밀번호 찾기 실패",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@FindIdPwActivity,
                                "서버 오류: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@FindIdPwActivity,
                            "네트워크 오류: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // 날짜 선택 Dialog (아이디 찾기)
    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val sel = Calendar.getInstance()
                sel.set(year, month, dayOfMonth)
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.dateText.setText(fmt.format(sel.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // 여러 이메일을 다음 화면으로 전달
    private fun moveToIdResult(userName: String, emailList: ArrayList<String>) {
        val intent = Intent(this, IdResultActivity::class.java).apply {
            putExtra("userName", userName)
            putStringArrayListExtra("emailList", emailList)
        }
        startActivity(intent)
    }

    private fun moveToPwResult(userEmail: String) {
        val intent = Intent(this, NewPwActivity::class.java).apply {
            putExtra("userEmail", userEmail)
        }
        startActivity(intent)
    }
}
