package com.example.moneychanger.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.adapter.ListAdapter
import com.example.moneychanger.R
import com.example.moneychanger.calendar.CalendarActivity
import com.example.moneychanger.calendar.DashboardActivity
import com.example.moneychanger.setting.SettingActivity
import com.example.moneychanger.databinding.ActivityMainBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.etc.OnStoreNameUpdatedListener
import com.example.moneychanger.etc.SlideNewList
import com.example.moneychanger.location.LocationActivity
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.currency.CurrencyViewModel
import com.example.moneychanger.network.list.ListsResponseDto
import com.example.moneychanger.network.currency.CurrencyResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.onboarding.LoginSelectActivity
import com.example.moneychanger.onboarding.find.NewPwActivity
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.absoluteValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity(), OnStoreNameUpdatedListener {
    private lateinit var binding:  ActivityMainBinding
    private lateinit var adapter: ListAdapter
    private lateinit var currencyViewModel: CurrencyViewModel
    private var lists: MutableList<ListModel> = mutableListOf()

    private lateinit var addListLauncher: ActivityResultLauncher<Intent>
    private lateinit var editListLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // list 직접 추가하기 버튼 클릭 이벤트 설정
        binding.buttonAdd.setOnClickListener{
            val slideNewList = SlideNewList()
            slideNewList.show(supportFragmentManager, slideNewList.tag)
        }

        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener {
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity::class.java)
            addListLauncher.launch(intent)
        }

        // recyclerView 연결
        adapter = ListAdapter(lists) { item ->
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("list_id", item.listId)
            intent.putExtra("currencyIdFrom", item.currencyFrom.currencyId)
            intent.putExtra("currencyIdTo", item.currencyTo.currencyId)
            editListLauncher.launch(intent)
        }
        binding.listContainer.layoutManager = LinearLayoutManager(this)
        binding.listContainer.adapter = adapter

        //팝업 띄우기
        showAccessPopup()

        currencyViewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]
        fetchAndStoreCurrencyData {
            fetchListsFromApi()
        }

        // 임시 버튼 연결 - 임시
        binding.b1.setOnClickListener {
            // 로그인 선택 페이지로 연결
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        binding.b2.setOnClickListener{
            val intent = Intent(this, NewPwActivity::class.java)
            startActivity(intent)
        }
        binding.b3.setOnClickListener{
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }
        binding.b4.setOnClickListener{
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        // 직접 리스트 추가 후 갱신
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val listAdded = bundle.getBoolean("listAdded")
            if (listAdded) {
                fetchListsFromApi()
            }
        }

        addListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                fetchListsFromApi()
            }
        }
        editListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                fetchListsFromApi()
            }
        }

    }
    private fun fetchAndStoreCurrencyData(onFinished: () -> Unit) {
        apiService.findAll().enqueue(object : Callback<ApiResponse<List<CurrencyResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<CurrencyResponseDto>>>,
                response: Response<ApiResponse<List<CurrencyResponseDto>>>
            ) {
                if (response.isSuccessful) {
                    val dtoList = response.body()?.data
                    if (dtoList != null) {
                        val currencyList = dtoList.map {
                            CurrencyModel(
                                currencyId = it.currencyId,
                                curUnit = it.curUnit,
                                dealBasR = it.dealBasR.toDoubleOrNull() ?: 0.0,
                                curNm = it.curNm
                            )
                        }
                        CurrencyManager.setCurrencies(currencyList)
                        Log.d("CurrencyManager", "환율 데이터 저장 완료 (${currencyList.size}개)")
                    }
                }
                // ★ 반드시 응답 이후에 리스트 가져오기
                onFinished()
            }

            override fun onFailure(call: Call<ApiResponse<List<CurrencyResponseDto>>>, t: Throwable) {
                onFinished()
            }
        })
    }


    // 직접 리스트 추가하기 관련 함수
    // 콜백: SlideEdit에서 storeName을 전달받으면 실행됨
    override fun onStoreNameUpdated(storeName: String) {
        val listId = UUID.randomUUID().mostSignificantBits.absoluteValue // 아이디 임시 생성 -> UUID

        val newItem = CurrencyManager.getById(1)?.let {
            CurrencyManager.getById(2)?.let { it1 ->
                ListModel(
                    userId = 1,
                    listId = listId,
                    name = storeName,
                    createdAt = getCurrentDateTime(),  // "yyyy-MM-dd HH:mm:ss"
                    location = "", // 위치는 일단 공백으로 들어가게
                    deletedYn = false,
                    currencyFrom = it,
                    currencyTo = it1
                )
            }
        }
        // 나중에 여기에 db에 추가하는 부분 넣으면 됨.
        if (newItem != null) {
            adapter.addItem(newItem)
        }
    }

    // 현재 시간으로 created At 생성 함수
    fun getCurrentDateTime(): String {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_setting -> { // 설정 버튼 클릭 처리
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.button_delete -> {
                adapter.toggledeleteMode()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //팝업 띄우는 함수
    private fun showAccessPopup() {
        val dialogView = layoutInflater.inflate(R.layout.access_popup, null) // 팝업 레이아웃 inflate
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme) // 팝업 테마 적용
            .setView(dialogView)
            .create()

        // 버튼 클릭 이벤트
        val buttonSubmit = dialogView.findViewById<LinearLayout>(R.id.button_submit)
        buttonSubmit.setOnClickListener {
            dialog.dismiss() // 팝업 닫기
        }

        dialog.show()

        // 팝업 크기 설정
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    private fun fetchListsFromApi() {
        apiService.getAllLists().enqueue(object : Callback<ApiResponse<List<ListsResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<ListsResponseDto>>>,
                response: Response<ApiResponse<List<ListsResponseDto>>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == "success") {
                        val dtoLists = responseBody.data ?: emptyList()

                        val updatedLists = mutableListOf<ListModel>()

                        dtoLists.forEach { dto ->
                            val currencyFrom = CurrencyManager.getById(dto.currencyFromId)
                            val currencyTo = CurrencyManager.getById(dto.currencyToId)

                            if (currencyFrom == null || currencyTo == null) {
                                Log.e("MainActivity", "⚠️ 통화 정보 매핑 실패: from=${dto.currencyFromId}, to=${dto.currencyToId}")
                                return@forEach
                            }

                            if (!dto.deletedYn) {
                                val updatedList = ListModel(
                                    listId = dto.listId,
                                    name = dto.name,
                                    createdAt = dto.createdAt,
                                    location = dto.location,
                                    deletedYn = false,
                                    currencyFrom = currencyFrom,
                                    currencyTo = currencyTo,
                                    userId = dto.userId
                                )
                                updatedLists.add(updatedList)
                            }
                        }

                        Log.d("MainActivity", "리스트 개수: ${updatedLists.size}")
                        runOnUiThread {
                            adapter.updateList(updatedLists)
                        }
                    } else {
                        Log.e("Retrofit", "응답 status 실패: ${responseBody?.message}")
                    }
                } else {
                    Log.e("Retrofit", "응답 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<ListsResponseDto>>>, t: Throwable) {
                Log.e("Retrofit", "API 호출 실패", t)
            }
        })
    }
}
