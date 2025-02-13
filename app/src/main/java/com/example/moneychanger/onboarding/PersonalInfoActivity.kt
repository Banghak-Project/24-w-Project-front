package com.example.moneychanger.onboarding

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityPersonalInfoBinding
import java.util.Calendar

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바 숨기기

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
        // PolicyActivity에서 전달된 데이터 받기
        val agreedTerms = listOf(
            intent.getBooleanExtra("checkboxFirst", false),
            intent.getBooleanExtra("checkboxSecond", false),
            intent.getBooleanExtra("checkboxThird", false)
        )

        Log.d("PersonalInfoActivity", "받은 동의 데이터: $agreedTerms")


        // LoginAuthActivity에서 이메일 받아오기
        val email = intent.getStringExtra("EMAIL_KEY") ?: ""
        binding.inputEmail.setText(email) // 받아온 이메일 자동 입력

        // 다음 버튼 클릭 시 로그인 선택 페이지로 이동
        binding.buttonNext.setOnClickListener {
            val intent = Intent(this, LoginSelectActivity::class.java)
            startActivity(intent)
        }

        // 성별 버튼 선택 이벤트
        binding.buttonMale.setOnClickListener { Log.d("Debug", "Male selected") }
        binding.buttonFemale.setOnClickListener { Log.d("Debug", "Female selected") }

        // 날짜 선택 기능
        setupDatePicker()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, null, year, month, day)
        datePickerDialog.datePicker.init(year, month, day) { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%02d", selectedYear % 100, selectedMonth + 1, selectedDay)
            binding.dateText.setText(formattedDate)
            datePickerDialog.dismiss()
        }

        binding.dateText.setOnClickListener {
            datePickerDialog.show()
        }
    }
}
