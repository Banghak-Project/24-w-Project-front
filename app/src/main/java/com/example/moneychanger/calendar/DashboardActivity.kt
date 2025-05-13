package com.example.moneychanger.calendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityDashboardBinding
import com.example.moneychanger.network.product.ProductModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var productList: List<ProductModel>

    private var currentYear = 0
    private var currentMonth = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 샘플용
        productList = getProductListFromSource()

        val today = LocalDate.now()
        val initialWeek = DateUtils.getWeekOfMonth(today)
        currentYear = today.year
        currentMonth = today.monthValue
        updateChartForWeek(initialWeek)

        binding.first.setOnClickListener { updateChartForWeek(1) }
        binding.second.setOnClickListener { updateChartForWeek(2) }
        binding.third.setOnClickListener { updateChartForWeek(3) }
        binding.forth.setOnClickListener { updateChartForWeek(4) }
        binding.fifth.setOnClickListener { updateChartForWeek(5) }

        // 왼쪽 화살표(이전 달)
        binding.leftArrow.setOnClickListener {
            val (newYear, newMonth) = DateUtils.moveToPreviousMonth(currentYear, currentMonth)
            currentYear = newYear
            currentMonth = newMonth

            updateChartForWeek(1)  // ← 예: 첫째 주로 리셋
            updateHeaderTexts()
        }

        // 오른쪽 화살표(다음 달)
        binding.rightArrow.setOnClickListener {
            val (newYear, newMonth) = DateUtils.moveToNextMonth(currentYear, currentMonth)
            currentYear = newYear
            currentMonth = newMonth

            updateChartForWeek(1)  // ← 예: 첫째 주로 리셋
            updateHeaderTexts()
        }

        updateHeaderTexts()

    }
    private fun updateHeaderTexts() {
        binding.calendarYearText.text = currentYear.toString()
        binding.calendarMonthText.text = currentMonth.toString()
    }

    private fun updateChartForWeek(week:Int) {
        Log.d("chart", "productList.size = ${productList.size}")

        val selectedYear = currentYear
        val selectedMonth = currentMonth

        val formatter = DateTimeFormatter.ISO_DATE_TIME  // createdAt 파싱용
        val (start, end) = DateUtils.getWeekRange(selectedYear, selectedMonth, week)

        val filteredProducts = productList.filter {
            val date = LocalDateTime.parse(it.createdAt, formatter).toLocalDate()
            !it.deletedYn && !date.isBefore(start) && !date.isAfter(end)
        }
        Log.d("chart", "filteredProducts.size = ${filteredProducts.size}")

        val daySums = mutableMapOf<DayOfWeek, Double>().apply {
            DayOfWeek.entries.forEach { this[it] = 0.0 }
        }

        for (product in filteredProducts) {
            val date = LocalDateTime.parse(product.createdAt, formatter).toLocalDate()
            val day = date.dayOfWeek
            daySums[day] = daySums[day]!! + product.originPrice
        }

        val entries = DayOfWeek.entries.toTypedArray().mapIndexed { index, day ->
            BarEntry(index.toFloat(), daySums[day]?.toFloat() ?: 0f)
        }

        val dataSet = BarDataSet(entries, "")
        binding.barChart.legend.isEnabled = false
        dataSet.color = ContextCompat.getColor(this, R.color.main)
        val barData = BarData(dataSet)

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // 🔽 아래쪽에 위치
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        binding.barChart.description.isEnabled = false

        val yAxisLeft = binding.barChart.axisLeft
        yAxisLeft.isEnabled = false
        yAxisLeft.setDrawGridLines(false)                // 그리드 보이기

        binding.barChart.axisRight.isEnabled = false    // 오른쪽 Y축 숨기기

        binding.barChart.data = barData
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("월", "화", "수", "목", "금", "토", "일"))
        binding.barChart.invalidate()

        binding.startMonth.text = start.monthValue.toString().padStart(2, '0')
        binding.startDate.text = start.dayOfMonth.toString().padStart(2, '0')

        binding.endMonth.text = end.monthValue.toString().padStart(2, '0')
        binding.endDate.text = end.dayOfMonth.toString().padStart(2, '0')
    }

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
}