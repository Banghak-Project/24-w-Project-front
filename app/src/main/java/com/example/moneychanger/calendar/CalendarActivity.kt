package com.example.moneychanger.calendar

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.adapter.CalendarDateAdapter
import com.example.moneychanger.adapter.HistoryAdapter
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.databinding.ActivityCalendarBinding
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.product.ProductModel
import java.time.LocalDate
import java.time.YearMonth

class CalendarActivity : AppCompatActivity() {
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var allProducts: List<ProductModel>
    private lateinit var allLists: List<ListModel>


    private lateinit var binding: ActivityCalendarBinding
    private var currentYear = 0
    private var currentMonth = 0
    private var selectedDate: LocalDate? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 오늘 날짜로 초기화
        val today = LocalDate.now()
        currentYear = today.year
        currentMonth = today.monthValue
        selectedDate = today

        // 전체 product 데이터 불러오기 (예시 함수)
        allProducts = getProductListFromSource()
        // 전체 list 데이터 불러오기 (예시 함수)
        allLists = getListModelListFromSource()

        // 왼쪽 화살표(이전 달)
        binding.leftArrow.setOnClickListener {
            val (newYear, newMonth) = DateUtils.moveToPreviousMonth(currentYear, currentMonth)
            currentYear = newYear
            currentMonth = newMonth
        }

        // 오른쪽 화살표(다음 달)
        binding.rightArrow.setOnClickListener {
            val (newYear, newMonth) = DateUtils.moveToNextMonth(currentYear, currentMonth)
            currentYear = newYear
            currentMonth = newMonth

        }

        historyAdapter = HistoryAdapter(emptyList(),emptyList())
        binding.historyList.layoutManager = LinearLayoutManager(this)
        binding.historyList.adapter = historyAdapter

        // 오늘 날짜 초기 필터링
        onDateSelected(LocalDate.now())

        // 화면 처음 세팅
        setCalendar()

    }

    private fun setCalendar() {
        val dateList = DateUtils.generateDateList(currentYear, currentMonth)
        val recordCountMap = allProducts
            .filter { !it.deletedYn }
            .groupingBy {
                LocalDate.parse(it.createdAt.substring(0, 10))
            }
            .eachCount()

        binding.date.layoutManager = GridLayoutManager(this, 7)
        binding.date.adapter = CalendarDateAdapter(
            dateList = dateList,
            recordCountMap = recordCountMap,
            selectedDate = selectedDate,  // 클릭된 날짜 넘김
            onDateClick = { clickedDate ->
                selectedDate = clickedDate
                onDateSelected(clickedDate)
                setCalendar()  // UI 갱신
            }
        )

        binding.calendarYearText.text = currentYear.toString()
        binding.calendarMonthText.text = currentMonth.toString()
    }

    private fun onDateSelected(date: LocalDate) {
        val selectedDateStr = date.toString() // 예: "2025-04-30"

        val filteredList = allProducts.filter {
            it.createdAt.startsWith(selectedDateStr) && !it.deletedYn
        }

        Log.d("Calendar", "Clicked date: $selectedDateStr")

        // 날짜 표시 UI 갱신
        binding.historyMonthText.text = date.monthValue.toString().padStart(2, '0')
        binding.historyDateText.text = date.dayOfMonth.toString().padStart(2, '0')

        // RecyclerView에 업데이트
        historyAdapter.updateList(filteredList, allLists)

    }


    // 샘플용
    private fun getProductListFromSource(): List<ProductModel> {
        return listOf(
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-04-30T10:23:45"
            ),
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과2",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-04-30T10:23:45"
            ),
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과3",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-04-30T10:23:45"
            ),
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과4",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-04-30T10:23:45"
            ),
            ProductModel(
                productId = 2L,
                listId = 101L,
                name = "바나나",
                originPrice = 2000.0,
                deletedYn = false,
                createdAt = "2025-04-30T12:11:07"
            ),
            ProductModel(
                productId = 3L,
                listId = 102L,
                name = "우유",
                originPrice = 1200.0,
                deletedYn = false,
                createdAt = "2025-04-30T09:00:00"
            ),
            ProductModel(
                productId = 4L,
                listId = 102L,
                name = "커피",
                originPrice = 3000.0,
                deletedYn = false,
                createdAt = "2025-04-29T18:45:00"
            )
        )
    }

    private fun getListModelListFromSource(): List<ListModel> {
        return listOf(
            ListModel(
                listId = 101L,
                name = "명지대 마트",
                userId = 1L,
                createdAt = "2025-04-01T10:00:00",
                location = "서울",
                currencyFrom = CurrencyModel(
                    currencyId = 1L,
                    curUnit = "USD",
                    dealBasR = 1350.5,
                    curNm = "달러"
                ),
                currencyTo = CurrencyModel(
                    currencyId = 2L,
                    curUnit = "JPY",
                    dealBasR = 1.0,
                    curNm = "엔"
                ),
                deletedYn = false
            ),
            ListModel(
                listId = 102L,
                name = "편의점",
                userId = 1L,
                createdAt = "2025-04-02T11:30:00",
                location = "부산",
                currencyFrom = CurrencyModel(
                    currencyId = 3L,
                    curUnit = "JPY",
                    dealBasR = 9.75,
                    curNm = "엔"
                ),
                currencyTo = CurrencyModel(
                    currencyId = 2L,
                    curUnit = "KRW",
                    dealBasR = 1.0,
                    curNm = "원"
                ),
                deletedYn = false
            )
        )
    }

}








