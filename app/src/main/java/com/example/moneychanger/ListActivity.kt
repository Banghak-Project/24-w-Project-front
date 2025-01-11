package com.example.moneychanger

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.databinding.ActivityListBinding

class ListActivity : AppCompatActivity(){
    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.pageText.text = "상품 리스트"

        binding.spinnerContainer.setOnClickListener {
            binding.spinner.performClick()
            Log.d("Debug","스피너 클릭됨")
        }

        // Spinner 데이터 설정
        val items = listOf("최신순", "가격순")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        binding.spinner.adapter = adapter

        // Spinner 항목 선택 이벤트
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // 선택된 항목 처리
                val selectedOption = items[position]
                binding.spinnerText.text = selectedOption
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택된 항목이 없을 때의 처리 (옵션)
            }
        }


    }
}