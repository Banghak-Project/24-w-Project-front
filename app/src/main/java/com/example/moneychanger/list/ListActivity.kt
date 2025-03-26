package com.example.moneychanger.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.moneychanger.camera.CameraActivity2
import com.example.moneychanger.etc.SlideEdit
import com.example.moneychanger.databinding.ActivityListBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.ListsResponseDto
import com.example.moneychanger.etc.DataProvider
import com.example.moneychanger.network.CurrencyStoreManager
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListActivity : AppCompatActivity(), OnStoreNameUpdatedListener {
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: CurrencyViewModel

    private var currencyIdFrom = 23L // 더미 데이터 (리스트 정보 가져와야 함)
    private var currencyIdTo = 14L // 더미 데이터 (리스트 정보 가져와야 함)
    private val userId = TokenManager.getUserId() ?: -1L
    private val location = "Seoul"

    private var productList: MutableList<ProductModel> = mutableListOf()
    private var selectedList: ListModel? = null

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

        // 직접 추가하기 버튼 클릭 이벤트 처리
        binding.buttonAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("currencyIdFrom", currencyIdFrom)
            intent.putExtra("currencyIdTo", currencyIdTo)
            intent.putExtra("listId", selectedListId)
            startActivity(intent)
        }

        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener {
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity2::class.java)
            intent.putExtra("listId", selectedListId)
            intent.putExtra("currencyIdFrom", currencyIdFrom)
            intent.putExtra("currencyIdTo", currencyIdTo)
            startActivity(intent)
        }
        // 인텐트에서 list_id 받아오기
        val selectedListId = intent.getLongExtra("list_id", 0L)
        fetchListByIdFromApi(selectedListId) { list ->
            if (list != null) {
                selectedList = list // selectedList 초기화
                updateUI(list) // UI 업데이트
                binding.productContainer.layoutManager = LinearLayoutManager(this)
                binding.productContainer.adapter = ProductAdapter(productList.toMutableList())
                fetchProductsByListId(selectedListId)
            } else {
                Toast.makeText(this, "리스트 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 삭제하기 버튼 클릭 이벤트 처리
        binding.buttonMoveToDelete.setOnClickListener {
            val intent = Intent(this, DeleteActivity::class.java)
            intent.putExtra("list_id", selectedListId)
            startActivity(intent)
        }
    }

    private fun fetchProductsByListId(listId: Long){
        val apiService = RetrofitClient.apiService

        apiService.getProductByListsId(listId).enqueue(object : Callback<ApiResponse<List<ProductResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<ProductResponseDto>>>,
                response: Response<ApiResponse<List<ProductResponseDto>>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val gson = Gson()
                    val jsonElement = gson.toJsonTree(response.body()?.data)
                    val productDtoList = gson.fromJson(jsonElement, Array<ProductResponseDto>::class.java).toList()

                    val products = productDtoList.map {
                        ProductModel(
                            productId = it.productId,
                            name = it.name,
                            originPrice = it.originPrice,
                            listId = it.listId,
                            deletedYn = it.deletedYn
                        )
                    }

                    Log.d("ListActivity", "상품 불러오기 성공: $products")

                    productList = products.toMutableList()
                    binding.productContainer.layoutManager = LinearLayoutManager(this@ListActivity)
                    binding.productContainer.adapter = ProductAdapter(productList)
                } else {
                    Toast.makeText(this@ListActivity, "상품 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<ApiResponse<List<ProductResponseDto>>>, p1: Throwable) {
                Log.e("ListActivity", "API 실패", p1)
                Toast.makeText(this@ListActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStoreNameUpdated(storeName: String) {
        // SlideEdit에서 받은 데이터를 placeName TextView에 업데이트
        binding.placeName.text = storeName
    }
    private fun fetchListByIdFromApi(id: Long, callback: (ListModel?) -> Unit) {
        val apiService = RetrofitClient.apiService

        apiService.getListsById(id).enqueue(object : Callback<ApiResponse<ListsResponseDto?>> {
            override fun onResponse(
                call: Call<ApiResponse<ListsResponseDto?>>,
                response: Response<ApiResponse<ListsResponseDto?>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == "success" && responseBody.data != null) {
                        val gson = Gson()
                        val json = gson.toJson(responseBody.data)  // data를 JSON 문자열로 변환
                        try {
                            val dtoList = gson.fromJson(json, ListsResponseDto::class.java)
                            Log.d("DEBUG", "파싱된 리스트: $dtoList")

                            val currencyFrom = dtoList.currencyFrom
                            val currencyTo = dtoList.currencyTo

                            val listModel = ListModel(
                                listId = dtoList.listId,
                                name = dtoList.name,
                                userId = dtoList.userId,
                                location = dtoList.location,
                                createdAt = dtoList.createdAt,
                                currencyFrom = currencyFrom,
                                currencyTo = currencyTo,
                                deletedYn = dtoList.deletedYn
                            )
                            callback(listModel) // 데이터를 받은 후 콜백 실행
                        } catch (e: Exception) {
                            Log.e("GSON_ERROR", "파싱 실패", e)
                        }
                    } else {
                        Log.e("Retrofit", "응답이 null입니다.")
                        callback(null)
                    }
                } else {
                    Log.e("Retrofit", "응답 실패: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ListsResponseDto?>>, t: Throwable) {
                Log.e("Retrofit", "API 호출 실패", t)
                callback(null)
            }
        })
    }
    private fun updateUI(list: ListModel) {
        binding.placeName.text = list.name
        binding.locationName.text = list.location
        val dateTime = LocalDateTime.parse(list.createdAt, DateTimeFormatter.ISO_DATE_TIME)
        binding.createdDate.text = dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
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



