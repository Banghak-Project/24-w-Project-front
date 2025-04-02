package com.example.moneychanger.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.etc.CustomSpinner
import com.example.moneychanger.etc.OnStoreNameUpdatedListener
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.R
import com.example.moneychanger.adapter.ListAdapter
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.camera.CameraActivity2
import com.example.moneychanger.etc.SlideEdit
import com.example.moneychanger.databinding.ActivityListBinding
import com.example.moneychanger.etc.TotalAmountUtil
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyViewModel
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.ListsResponseDto
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.android.gms.common.api.Api
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListActivity : AppCompatActivity(), OnStoreNameUpdatedListener {
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: CurrencyViewModel

    private val userId = TokenManager.getUserId() ?: -1L
    private val location = "Seoul"

    private var productList: MutableList<ProductModel> = mutableListOf()
    private var selectedList: ListModel? = null

    private lateinit var addProductLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게


        // 인텐트에서 list_id 받아오기
        val selectedListId = intent.getLongExtra("list_id", 0L)
        var currencyIdFrom = intent.getLongExtra("currencyIdFrom", -1L)
        var currencyIdTo = intent.getLongExtra("currencyIdTo", -1L)

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
        val currencyList = CurrencyManager.getCurrencies()
        Log.i("ListActivity","hi2 $currencyList")
        if (currencyList.isEmpty()) {
            Toast.makeText(this, "통화를 불러오는데 실패했습니다.여기 확인", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 통화 Spinner 데이터 설정
        val currencyUnits: List<String> = currencyList?.map { it.curUnit } ?: emptyList()
        val customSpinner1 = CustomSpinner(this, currencyUnits)
        val customSpinner2 = CustomSpinner(this, currencyUnits)

        // 바꿀 통화 Spinner 항목 선택 이벤트
        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                // 1 >통화< 당 00 $
                binding.currencyName3.text = selected
                viewModel.updateCurrency(selected)

                val selectedCurrency = CurrencyManager.getByUnit(selected)
                currencyIdFrom = selectedCurrency.currencyId
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
                val selectedCurrency = CurrencyManager.getByUnit(selected)
                currencyIdTo = selectedCurrency.currencyId
            }
        }

        // 장소 수정하기 버튼 클릭 이벤트 처리
        binding.buttonEdit.setOnClickListener {
            val slideEdit = SlideEdit()
            slideEdit.show(supportFragmentManager, slideEdit.tag)
        }

        // 직접 추가하기 버튼 클릭 이벤트 처리
        binding.buttonAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java).apply {
                putExtra("currencyIdFrom", currencyIdFrom)
                putExtra("currencyIdTo", currencyIdTo)
                putExtra("listId", selectedList!!.listId)
            }
            addProductLauncher.launch(intent)
        }


        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener {
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity2::class.java)
            intent.putExtra("listId", selectedList!!.listId)
            intent.putExtra("currencyIdFrom", currencyIdFrom)
            intent.putExtra("currencyIdTo", currencyIdTo)

            addProductLauncher.launch(intent)
        }

//        fetchListByIdFromApi(selectedListId) { list ->
//            if (list != null) {
//                Toast.makeText(this, "${selectedListId}", Toast.LENGTH_SHORT).show()
//                selectedList = list // selectedList 초기화
//                updateUI(list) // UI 업데이트
//                binding.productContainer.layoutManager = LinearLayoutManager(this)
//                binding.productContainer.adapter = ProductAdapter(
//                    productList.toMutableList(),
//                    selectedList!!.currencyFrom.currencyId,
//                    selectedList!!.currencyTo.currencyId)
//                fetchProductsByListId(selectedListId)
//            } else {
//                Toast.makeText(this, "리스트 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }

        fetchListByIdFromApi(selectedListId) { list ->
            list?.let {
                Toast.makeText(this, "$selectedListId", Toast.LENGTH_SHORT).show()
                Log.d("ListDebug", "Fetched List: $it")

                selectedList = it // selectedList 초기화
                updateUI(it) // UI 업데이트
//                binding.productContainer.layoutManager = LinearLayoutManager(this)
//                binding.productContainer.adapter = ProductAdapter(
//                    productList.toMutableList(),
//                    it.currencyFrom.currencyId,
//                    it.currencyTo.currencyId
//                )
                fetchProductsByListId(selectedListId)
            }
        }


        // 삭제하기 버튼 클릭 이벤트 처리
        binding.buttonMoveToDelete.setOnClickListener {
            val intent = Intent(this, DeleteActivity::class.java)
            intent.putExtra("list_id", selectedListId)
            startActivity(intent)
        }

        addProductLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 새로 상품 목록을 불러옴!
                selectedList?.let {
                    fetchProductsByListId(it.listId)
                }
            }
        }
    }

    private fun fetchProductsByListId(listId: Long) {
        apiService.getProductByListsId(listId).enqueue(object : Callback<ApiResponse<List<ProductResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<ProductResponseDto>>>,
                response: Response<ApiResponse<List<ProductResponseDto>>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val productDtoList = response.body()?.data ?: emptyList()

                    Log.d("ListActivity", "응답 파싱 성공: $productDtoList")

                    val products = productDtoList.map {
                        ProductModel(
                            productId = it.productId,
                            name = it.name,
                            originPrice = it.originPrice,
                            listId = it.listId,
                            deletedYn = it.deletedYn
                        )
                    }

                    productList = products.toMutableList()

                    binding.productContainer.layoutManager = LinearLayoutManager(this@ListActivity)
                    binding.productContainer.adapter = ProductAdapter(
                        productList,
                        selectedList!!.currencyFrom.currencyId,
                        selectedList!!.currencyTo.currencyId
                    )

//                    val adapter = binding.productContainer.adapter as? ProductAdapter
//                    if (adapter != null) {
//                        adapter.updateProducts(productList)
//                    } else {
//                        binding.productContainer.layoutManager = LinearLayoutManager(this@ListActivity)
//                        binding.productContainer.adapter = ProductAdapter(
//                            productList,
//                            selectedList!!.currencyFrom.currencyId,
//                            selectedList!!.currencyTo.currencyId
//                        )
//                    }

                    // 총 금액 계산
                    val total = productList.sumOf { it.originPrice ?: 0.0 }
                    binding.totalSum.text = total.toString()

                } else {
                    Toast.makeText(this@ListActivity, "상품 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<ProductResponseDto>>>, t: Throwable) {
                Log.e("ListActivity", "API 실패", t)
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
                    val data = response.body()?.data
                    if (data != null) {
                            val currencyFrom = CurrencyManager.getById(data.currencyFromId)
                            val currencyTo = CurrencyManager.getById(data.currencyToId)

                            if (currencyFrom != null && currencyTo != null) {
                                val listModel = ListModel(
                                    listId = data.listId,
                                    name = data.name,
                                    userId = data.userId,
                                    location = data.location,
                                    createdAt = data.createdAt,
                                    currencyFrom = currencyFrom,
                                    currencyTo = currencyTo,
                                    deletedYn = data.deletedYn
                                )
                                callback(listModel)
                            }else {
                                Log.e("ListActivity", "통화 정보 매핑 실패")
                                callback(null)
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
        binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분 ss초"))
        binding.currencyName1.text = list.currencyFrom.curUnit
        binding.currencyName2.text = list.currencyTo.curUnit
        binding.currencyName3.text = list.currencyFrom.curUnit

        val symbolKey = list.currencyTo.curUnit.replace(Regex("\\(.*\\)"), "").trim()
        val resId = resources.getIdentifier(symbolKey, "string", packageName)
        val symbolText = if (resId != 0) getString(resId) else symbolKey // fallback
        binding.currencySymbol1.text = symbolText
        binding.currencySymbol2.text = symbolText

        binding.totalSum.text = calculateTotalAmount().toString()
    }

    private fun calculateTotalAmount(): Double {
        return productList.sumOf { it.originPrice ?: 0.0 }
    }


}





