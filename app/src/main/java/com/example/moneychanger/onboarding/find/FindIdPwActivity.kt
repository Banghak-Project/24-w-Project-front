package com.example.moneychanger.onboarding.find

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityFindIdPwBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.network.RetrofitClient
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
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        // 생년월일 선택 (아이디 찾기용)
        binding.dateText.setOnClickListener{
            showDatePickerDialog()
        }

        // 생년월일 선택 (비밀번호 찾기용)
        binding.dateTextPw.setOnClickListener{
            showDatePickerDialogForPw()
        }

        // 상단 버튼 선택 이벤트
        binding.btnFindId.setOnClickListener {
            Log.d("Debug", "아이디 버튼 클릭")
        }
        binding.btnResetPassword.setOnClickListener {
            Log.d("Debug", "비번 버튼 클릭")
        }
        binding.idText.setOnClickListener {
            binding.layoutFindID.visibility = View.VISIBLE
            binding.layoutFindPW.visibility = View.GONE

            binding.btnFindId.visibility = View.VISIBLE
            binding.idText.visibility = View.INVISIBLE
            binding.btnResetPassword.visibility = View.INVISIBLE
            binding.pwText.visibility = View.VISIBLE
            Log.d("Debug", "아이디 찾기 클릭")
        }
        binding.pwText.setOnClickListener {
            binding.layoutFindID.visibility = View.GONE
            binding.layoutFindPW.visibility = View.VISIBLE

            binding.btnFindId.visibility = View.INVISIBLE
            binding.idText.visibility = View.VISIBLE
            binding.btnResetPassword.visibility = View.VISIBLE
            binding.pwText.visibility = View.INVISIBLE
            Log.d("Debug", "비밀번호 재설정 클릭")
        }
        // ID 찾기 요청
        binding.buttonToIdResult.setOnClickListener {
            val userName = binding.editTextName.text.toString()
            val birthDate = binding.dateText.text.toString()

            if (userName.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.findId(userName, birthDate)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.status == "success") {
                                val userEmail = apiResponse.data.toString()
                                moveToIdResult(userName, userEmail)
                            } else {
                                Toast.makeText(this@FindIdPwActivity, apiResponse?.message ?: "아이디 찾기 실패", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@FindIdPwActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FindIdPwActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 비밀번호 찾기 요청
        binding.buttonToPwResult.setOnClickListener {
            val userName = binding.editTextNamePw.text.toString()
            val userEmail = binding.dateTextPw.text.toString()

            if (userName.isEmpty() || userEmail.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.findPassword(userEmail, userName)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.status == "success") {
                                moveToPwResult()
                            } else {
                                Toast.makeText(this@FindIdPwActivity, apiResponse?.message ?: "비밀번호 찾기 실패", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@FindIdPwActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FindIdPwActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.dateText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // 날짜 선택 Dialog (비밀번호 찾기)
    private fun showDatePickerDialogForPw() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.dateTextPw.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }


    private fun moveToIdResult(userName: String, userEmail: String) {
        val intent = Intent(this, IdResultActivity::class.java)
        intent.putExtra("userName", userName)
        intent.putExtra("userEmail", userEmail)
        startActivity(intent)
    }

    private fun moveToPwResult() {
        val intent = Intent(this, PwResultActivity::class.java)
        startActivity(intent)
    }
}