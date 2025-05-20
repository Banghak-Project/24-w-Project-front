package com.example.moneychanger.calendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityDashboardBinding
import com.example.moneychanger.etc.ExchangeRateUtil.calculateExchangeRate
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.product.ProductWithCurrencyDto
import com.example.moneychanger.network.user.ApiResponse
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.floor

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var productList: List<ProductWithCurrencyDto>
    private lateinit var weekLayouts: List<LinearLayout>

    private var userDefaultCurrency = 0.toLong()
    private var currentYear = 0
    private var currentMonth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        getUserDefaultCurrency { defaultCurrency ->
            if (defaultCurrency != 0.toLong() && defaultCurrency != null) {
                userDefaultCurrency = defaultCurrency
                Log.d("통화", "사용자 통화 ID: $defaultCurrency")

                val curModel = CurrencyManager.getById(defaultCurrency)
                    ?: error("Unknown currency id=$defaultCurrency")
                val codeKey = curModel.curUnit
                    .replace(Regex("\\(.*\\)"), "")
                    .trim()
                val resId = resources.getIdentifier(codeKey, "string", packageName)
                val symbol = if (resId != 0) getString(resId) else codeKey

                binding.weeklyCurrencyTextView.text = symbol

            } else {
                Toast.makeText(this, "기본 통화 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            getProductList { result ->
                productList = result
                Log.i("m",productList.toString())
                val today = LocalDate.now()
                val initialWeek = DateUtils.getWeekOfMonth(today)
                currentYear = today.year
                currentMonth = today.monthValue
                updateWeeklyChart(initialWeek)

                weekLayouts = listOf(
                    binding.first,
                    binding.second,
                    binding.third,
                    binding.forth,
                    binding.fifth
                )

                weekLayouts.forEachIndexed { index, layout ->
                    layout.setOnClickListener {
                        updateSelectedWeek(layout)
                        updateWeeklyChart(index + 1)
                    }
                }

                updateSelectedWeek(weekLayouts[initialWeek - 1])

                // 왼쪽 화살표(이전 달)
                binding.leftArrow.setOnClickListener {
                    val (newYear, newMonth) = DateUtils.moveToPreviousMonth(currentYear, currentMonth)
                    currentYear = newYear
                    currentMonth = newMonth

                    updateWeeklyChart(1)  // ← 예: 첫째 주로 리셋
                    updateHeaderTexts()
                    updateMonthlyChart()
                    updateYearlyChart()
                }

                // 오른쪽 화살표(다음 달)
                binding.rightArrow.setOnClickListener {
                    val (newYear, newMonth) = DateUtils.moveToNextMonth(currentYear, currentMonth)
                    currentYear = newYear
                    currentMonth = newMonth

                    updateWeeklyChart(1)  // ← 예: 첫째 주로 리셋
                    updateHeaderTexts()
                    updateMonthlyChart()
                    updateYearlyChart()
                }

                updateHeaderTexts()
                updateMonthlyChart()
                updateYearlyChart()
            }

        }



    }
    private fun updateSelectedWeek(selectedLayout: LinearLayout) {
        weekLayouts.forEach { layout ->
            if (layout == selectedLayout) {
                layout.setBackgroundResource(R.drawable.round_background2)
            } else {
                layout.background = null
            }
        }
    }
    private fun updateHeaderTexts() {
        binding.calendarYearText.text = currentYear.toString()
        binding.calendarMonthText.text = currentMonth.toString()
    }

    private fun updateWeeklyChart(week:Int) {
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
            daySums[day] = daySums[day]!! + calculateExchangeRate(product.currencyId, userDefaultCurrency, product.originPrice)
        }

        val entries = DayOfWeek.entries.toTypedArray().mapIndexed { index, day ->
            BarEntry(index.toFloat(), daySums[day]?.toFloat() ?: 0f)
        }

        val dataSet = BarDataSet(entries, "")
        binding.weekChart.legend.isEnabled = false
        dataSet.color = ContextCompat.getColor(this, R.color.main)
        val barData = BarData(dataSet)

        val xAxis = binding.weekChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM // 🔽 아래쪽에 위치
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(this, R.color.gray_07)
        binding.weekChart.description.isEnabled = false

        val yAxisLeft = binding.weekChart.axisLeft
        yAxisLeft.isEnabled = false
        yAxisLeft.setDrawGridLines(false)                // 그리드 보이기

        binding.weekChart.axisRight.isEnabled = false    // 오른쪽 Y축 숨기기

        binding.weekChart.data = barData
        binding.weekChart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("월", "화", "수", "목", "금", "토", "일"))
        binding.weekChart.invalidate()

        binding.startMonth.text = start.monthValue.toString().padStart(2, '0')
        binding.startDate.text = start.dayOfMonth.toString().padStart(2, '0')

        binding.endMonth.text = end.monthValue.toString().padStart(2, '0')
        binding.endDate.text = end.dayOfMonth.toString().padStart(2, '0')

        val totalWeeklySum = filteredProducts.sumOf { calculateExchangeRate(it.currencyId, userDefaultCurrency, it.originPrice) }
        binding.weeklyTotal.text = floor(totalWeeklySum).toString()

    }

    private fun updateMonthlyChart() {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val thisMonth = LocalDate.of(currentYear, currentMonth, 1)
        val lastMonth = thisMonth.minusMonths(1)

        // 지출 합계 계산
        fun sumForMonth(month: LocalDate): Double {
            return productList.filter {
                val date = LocalDateTime.parse(it.createdAt, formatter).toLocalDate()
                !it.deletedYn && date.year == month.year && date.month == month.month
            }.sumOf { calculateExchangeRate(it.currencyId, userDefaultCurrency, it.originPrice) }
        }

        val lastSum = sumForMonth(lastMonth)
        val thisSum = sumForMonth(thisMonth)

        val percentChange: Double
        val rounded: String
        if (lastSum == 0.0 && thisSum > 0.0) {
            percentChange = 100.0
            rounded = "100"
        } else if (lastSum == 0.0 && thisSum == 0.0) {
            percentChange = 0.0
            rounded = "0"
        } else {
            percentChange = ((thisSum - lastSum) / lastSum) * 100
            rounded = String.format("%.0f", kotlin.math.abs(percentChange))
        }

        binding.comapareMonthNum.text = when {
            percentChange > 0 -> rounded
            percentChange < 0 -> rounded
            else -> "0"
        }
        binding.compareMonthText.text = when {
            percentChange > 0 -> "상승"
            percentChange < 0 -> "하락"
            else -> "변화"
        }

        val labels = listOf("${lastMonth.monthValue}월", "${thisMonth.monthValue}월")

        // Bar chart entry
        val barEntries = listOf(
            BarEntry(0f, lastSum.toFloat()),
            BarEntry(1f, thisSum.toFloat())
        )
        val barDataSet = BarDataSet(barEntries, "지출 합계").apply {
            setColors(
                ContextCompat.getColor(this@DashboardActivity, R.color.gray_02),  // 지난달
                ContextCompat.getColor(this@DashboardActivity, R.color.main)      // 이번달
            )
            valueTextSize = 10f
        }
        val barData = BarData(barDataSet)
        barData.barWidth = 0.4f

        // Line chart entry
        val lineEntries = listOf(
            Entry(0f, lastSum.toFloat()),
            Entry(1f, thisSum.toFloat())
        )
        val lineDataSet = LineDataSet(lineEntries, "지출 추이").apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.blue_03)
            setCircleColor(ContextCompat.getColor(this@DashboardActivity, R.color.blue_03))
            circleRadius = 3f
            setDrawValues(false)
            setDrawCircles(true)
            setDrawCircleHole(false)
            setDrawFilled(false)
            mode = LineDataSet.Mode.LINEAR

        }
        val lineData = LineData(lineDataSet)

        // Combined chart
        val combinedData = CombinedData().apply {
            setData(barData)
            setData(lineData)
        }

        binding.monthChart.apply {
            data = combinedData
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                axisMinimum = -0.5f
                axisMaximum = 1.5f
                textColor = ContextCompat.getColor(context, R.color.gray_07)
            }
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            isHighlightPerTapEnabled = false
            isHighlightPerDragEnabled = false
            invalidate()
        }
    }

    private fun updateYearlyChart() {
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        val thisYear = currentYear
        val lastYear = thisYear - 1
        val yearList = listOf(lastYear, thisYear)

        val yearSumMap = yearList.associateWith { year ->
            productList
                .filter {
                    val date = LocalDateTime.parse(it.createdAt, formatter).toLocalDate()
                    !it.deletedYn && date.year == year
                }
                .sumOf { calculateExchangeRate(it.currencyId, userDefaultCurrency, it.originPrice) }
        }

        val lastAvg = yearSumMap[lastYear]
        val thisAvg = yearSumMap[thisYear]

        val epsilon = 0.0001
        val last = lastAvg ?: Double.NaN
        val thisY = thisAvg ?: Double.NaN

        val percentChange: Double
        binding.compareYearText.text = when {
            last.isNaN() && !thisY.isNaN() && thisY > epsilon -> {
                percentChange = 100.0
                "늘었어요"
            }
            last.isNaN() && (thisY.isNaN() || thisY <= epsilon) -> {
                percentChange = 0.0
                "없어요"
            }
            !last.isNaN() && !thisY.isNaN() -> {
                percentChange = ((thisY - last) / last) * 100
                when {
                    percentChange > epsilon -> "늘었어요"
                    percentChange < -epsilon -> "줄었어요"
                    else -> "같아요"
                }
            }
            else -> {
                "없어요"
            }
        }

        val entryLastYear = BarEntry(0f, yearSumMap[lastYear]?.toFloat() ?: 0f)
        val entryThisYear = BarEntry(1f, yearSumMap[thisYear]?.toFloat() ?: 0f)

        val setLast = BarDataSet(listOf(entryLastYear), "").apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.gray_03)
            valueTextColor = ContextCompat.getColor(this@DashboardActivity, R.color.gray_07)
        }

        val setThis = BarDataSet(listOf(entryThisYear), "").apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.main)
            valueTextColor = ContextCompat.getColor(this@DashboardActivity, R.color.white)
        }

        val barData = BarData(setLast, setThis)
        barData.barWidth = 0.5f
        binding.yearChart.data = barData


        binding.yearChart.apply {
            data = barData

            setDrawValueAboveBar(false)

            xAxis.apply {
                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(yearList.map { "${it}년" })
                textColor = ContextCompat.getColor(context, R.color.gray_07)
            }

            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            invalidate()
        }
    }

    private fun getProductList(onFinished: (List<ProductWithCurrencyDto>) -> Unit) {
        apiService.getAllProducts().enqueue(object : Callback<ApiResponse<List<ProductWithCurrencyDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<ProductWithCurrencyDto>>>,
                response: Response<ApiResponse<List<ProductWithCurrencyDto>>>
            ) {
                Log.d("DashboardActivity", "API 응답 성공: ${response.body()}")
                if (response.isSuccessful) {
                    val productList = response.body()?.data?: emptyList()
                    onFinished(productList)
                }else{
                    onFinished(emptyList())
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<List<ProductWithCurrencyDto>>>,
                t: Throwable
            ) {
                Log.e("DashboardActivity", "getProductList API 호출 실패: ${t.message}")
                onFinished(emptyList())
            }
        })
    }

    private fun getUserDefaultCurrency(onFinished: (Long?) -> Unit){
        apiService.getUserCurrency().enqueue(object : Callback<ApiResponse<Long>> {
            override fun onResponse(
                call:Call<ApiResponse<Long>>,
                response: Response<ApiResponse<Long>>
            ){
                if (response.isSuccessful){
                    val currencyId = response.body()?.data
                    onFinished(currencyId)
                }
                else{
                    Log.e("DashboardActivity", "통화 ID 조회 실패: ${response.code()}")
                    onFinished(null)
                }
            }

            override fun onFailure(p0: Call<ApiResponse<Long>>, t: Throwable) {
                Log.e("DashboardActivity", "API 호출 실패: ${t.message}")
                onFinished(null)
            }
        })
    }
}