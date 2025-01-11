package com.example.moneychanger

import android.app.DatePickerDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import com.example.moneychanger.databinding.ActivitySampleBinding
import java.util.Calendar

class SampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val datePicker: DatePicker = findViewById(R.id.datePicker)
        datePicker.setOnClickListener{
            Log.d("Debug", "클릭됨")
        }

        datePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            // 월(monthOfYear)는 0부터 시작하므로 +1 필요
            val selectedDate = "$year-${monthOfYear + 1}-$dayOfMonth"
            Log.d("Debug", "Selected date: $selectedDate")
        }

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