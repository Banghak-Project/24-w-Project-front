package com.example.moneychanger

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : TextView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // 다음
        binding.buttonNext.setOnClickListener {
            // 로그인 선택 페이지로 연결
            val intent = Intent(this, LoginSelectActivity::class.java)
            startActivity(intent)
        }

        binding.buttonMale.setOnClickListener {
            Log.d("Debug", "male")
        }

        // 날짜 선택 기능
        setupDatePicker()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // DatePickerDialog 생성
        val datePickerDialog = DatePickerDialog(this, null, year, month, day)

        // DatePicker 인스턴스 가져오기
        val datePicker = datePickerDialog.datePicker

        // 날짜 변경 리스너 설정
        datePicker.init(year, month, day) { _, selectedYear, selectedMonth, selectedDay ->
            Log.d("Debug", "Selected date: $selectedYear-${selectedMonth + 1}-$selectedDay")
            val formattedDate = String.format(
                "%02d/%02d/%02d",
                selectedYear % 100, selectedMonth + 1, selectedDay
            )
            binding.dateText.setText(formattedDate)

            // 날짜 선택 후 다이얼로그 닫기
            datePickerDialog.dismiss()
        }

        // 다이얼로그 보여주기
        binding.dateText.setOnClickListener {
            Log.d("Debug", "EditText clicked")
            datePickerDialog.show()
        }
    }

}