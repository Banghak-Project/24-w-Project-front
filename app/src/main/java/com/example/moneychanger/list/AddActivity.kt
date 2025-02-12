package com.example.moneychanger.list

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var viewModel: CurrencyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        binding.loginToolbar.pageText.text = "추가하기"

        // 통화 Spinner 데이터 설정
        val currencyItems = listOf("KRW", "JPY", "USD", "THB", "ITL", "UTC", "FRF", "GBP", "CHF", "VND", "AUD")
        val customSpinner1 = CustomSpinner(this, currencyItems)
        val customSpinner2 = CustomSpinner(this, currencyItems)

        // 바꿀 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                binding.currencyText1.text = selected
                val resourceId = resources.getIdentifier(selected, "string", packageName)
                binding.currencySymbol1.text = getString(resourceId)
                viewModel.updateCurrency(selected)
            }
        }

        // 바뀐 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                binding.currencyText2.text = selected
                val resourceId = resources.getIdentifier(selected, "string", packageName)
                binding.currencySymbol2.text = getString(resourceId)
                viewModel.updateCurrency(selected)
            }
        }
    }
}