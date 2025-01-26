package com.example.moneychanger.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityListBinding

class ListActivity : AppCompatActivity(){
    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        binding.loginToolbar.pageText.text = "상품 리스트"

        binding.spinnerContainer.setOnClickListener {
            binding.sortSpinner.performClick()
            Log.d("Debug","스피너 클릭됨")
        }

        // Spinner 데이터 설정
        val items = listOf("최신순", "가격순")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        binding.sortSpinner.adapter = adapter

        // Spinner 항목 선택 이벤트
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // 선택된 항목 처리
                val selectedOption = items[position]
                binding.sortSpinnerText.text = selectedOption
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택된 항목이 없을 때의 처리 (옵션)
            }
        }

        // 삭제하기 버튼 클릭 이벤트 처리
        binding.buttonMoveToDelete.setOnClickListener {
            val intent = Intent(this, DeleteActivity::class.java)
            startActivity(intent)
        }

        // 직접 추가하기 버튼 클릭 이벤트 처리
        binding.buttonAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener{
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }


    }
}