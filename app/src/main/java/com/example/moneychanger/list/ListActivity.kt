package com.example.moneychanger.list

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.etc.OnStoreNameUpdatedListener
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.R
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.etc.SlideEdit
import com.example.moneychanger.databinding.ActivityListBinding
import com.example.moneychanger.etc.DataProvider
import com.example.moneychanger.network.CurrencyStoreManager
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.product.ProductModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListActivity : AppCompatActivity(), OnStoreNameUpdatedListener {
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: CurrencyViewModel

    private var currencyIdFrom = 23L // 더미 데이터 (리스트 정보 가져와야 함)
    private var currencyIdTo = 14L // 더미 데이터 (리스트 정보 가져와야 함)
    private val userId = TokenManager.getUserId() ?: -1L
    private val location = "Seoul"

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

        // 통화 정보 가져오기
        val currencyList = CurrencyStoreManager.getCurrencyList()

        if (currencyList.isNullOrEmpty()) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_LONG).show()
            finish()  // 👉 종료하지 않고 onCreate 나감
            return
        }

        // 통화 Spinner 데이터 설정
        val currencyUnits: List<String> = currencyList?.mapNotNull { it.curUnit } ?: emptyList()
        val customSpinner1 = CustomSpinner(this, currencyUnits)
        val customSpinner2 = CustomSpinner(this, currencyUnits)

        // 바꿀 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                // 1 >통화< 당 00 $
                binding.currencyName3.text = selected
                viewModel.updateCurrency(selected)

                val selectedCurrency = CurrencyStoreManager.findCurrencyByUnit(selected)
                if (selectedCurrency != null) {
                    currencyIdFrom = selectedCurrency.currentId
                }
            }
        }

        // 바뀐 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                // 1 통화 당 00 >$<
                val cleanedSelected = selected.replace(Regex("\\(.*\\)"), "")
                val resourceId = resources.getIdentifier(cleanedSelected, "string", packageName)
                binding.currencySymbol1.text = getString(resourceId)
                // n0000 >$<
                binding.currencySymbol2.text = getString(resourceId)
                viewModel.updateCurrency(selected)

                val selectedCurrency = CurrencyStoreManager.findCurrencyByUnit(selected)
                if (selectedCurrency != null) {
                    currencyIdTo = selectedCurrency.currentId
                }
            }
        }


        // 장소 수정하기 버튼 클릭 이벤트 처리
        binding.buttonEdit.setOnClickListener {
            val slideEdit = SlideEdit()
            slideEdit.show(supportFragmentManager, slideEdit.tag)
        }


        // 인텐트에서 list_id 받아오기
        val selectedListId = intent.getLongExtra("list_id", 0L)

        // 직접 추가하기 버튼 클릭 이벤트 처리
        binding.buttonAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("currencyIdFrom", currencyIdFrom)
            intent.putExtra("currencyIdTo", currencyIdTo)
            intent.putExtra("listId", selectedListId)
            startActivity(intent)
        }

        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener{
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // 더미 데이터 - 임시
        val listData = DataProvider.listDummyModel
        val productData = DataProvider.productDummyModel

        // 선택된 list_id에 맞는 list 데이터 찾기
        val selectedList = listData.find { it.listId == selectedListId }
        // 선택된 list_id에 맞는 product 데이터 필터링
        val productList = productData.filter { product -> product.listId == selectedListId }

        // 삭제하기 버튼 클릭 이벤트 처리
        binding.buttonMoveToDelete.setOnClickListener {
            val intent = Intent(this, DeleteActivity::class.java)
            intent.putExtra("list_id", selectedListId)
            startActivity(intent)
        }

        // 아답터 사용하여 데이터 바인딩
        selectedList?.let {
            binding.placeName.text = it.name
            binding.locationName.text = it.location
            val dateTime = LocalDateTime.parse(it.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.createdDate.text = dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            // 디버깅 로그
            //Log.d("ListActivity", "Filtered products: $productList")

            // 하단(상품 부분 리사이클 뷰) 데이터 연결
            binding.productContainer.layoutManager = LinearLayoutManager(this)
            binding.productContainer.adapter = ProductAdapter(productList.toMutableList())
        } ?: run {
            // Log.e("ListActivity", "No list found for listId: $selectedListId")
            Toast.makeText(this, "리스트 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    override fun onStoreNameUpdated(storeName: String) {
        // SlideEdit에서 받은 데이터를 placeName TextView에 업데이트
        binding.placeName.text = storeName
    }

    // list와 관련있는 product만 걸러서 가져오기 위한 함수
    fun getProductsByListId(listId: Int): List<ProductModel> {
        return DataProvider.productDummyModel.filter { it.productId == listId }
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



