package com.example.moneychanger.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneychanger.CustomSpinner
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityListBinding

class ListActivity : AppCompatActivity(){
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: CurrencyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        binding.loginToolbar.pageText.text = "상품 리스트"

        // 최신순, 가격순 Spinner 데이터 설정
        val sortItems = listOf("최신순", "가격순")
        val sortSpinner = CustomSpinner(this, sortItems)

        // 최신순, 가격순 Spinner 항목 선택 이벤트
        binding.sortContainer.setOnClickListener {
            sortSpinner.show(binding.sortContainer) { selected ->
                binding.sortText.text = selected
            }
        }

        // 통화 Spinner 데이터 설정
        val currencyItems = listOf("KRW", "JPY", "USD", "THB", "ITL", "UTC", "FRF", "GBP", "CHF", "VND", "AUD")
        val customSpinner1 = CustomSpinner(this, currencyItems)
        val customSpinner2 = CustomSpinner(this, currencyItems)

        // 바꿀 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                // 1 >통화< 당 00 $
                binding.currencyName3.text = selected
                viewModel.updateCurrency(selected)
            }
        }

        // 바뀐 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                // 1 통화 당 00 >$<
                val resourceId = resources.getIdentifier(selected, "string", packageName)
                binding.currencySymbol1.text = getString(resourceId)
                // n0000 >$<
                binding.currencySymbol2.text = getString(resourceId)
                viewModel.updateCurrency(selected)
            }
        }

        // 나중에 list_product 아답터가 생기면... `RecyclerViewAdapter`에 ViewModel 전달
//        val adapter = CurrencyAdapter(viewModel)
//        binding.recyclerView.adapter = adapter


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

// list_product recylcerview에 통화 기호 전달하기 위한 클래스
class CurrencyViewModel : ViewModel() {
    private val _selectedCurrency = MutableLiveData<String>()
    val selectedCurrency: LiveData<String> get() = _selectedCurrency

    fun updateCurrency(currency: String) {
        _selectedCurrency.value = currency
    }
}