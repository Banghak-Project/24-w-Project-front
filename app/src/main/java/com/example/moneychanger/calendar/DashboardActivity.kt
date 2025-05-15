package com.example.moneychanger.calendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityDashboardBinding
import com.example.moneychanger.network.product.ProductModel
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
import com.github.mikephil.charting.formatter.ValueFormatter

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var productList: List<ProductModel>
    private lateinit var weekLayouts: List<LinearLayout>

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
            daySums[day] = daySums[day]!! + product.originPrice
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

        val totalWeeklySum = filteredProducts.sumOf { it.originPrice }
        binding.weeklyTotal.text = totalWeeklySum.toString()

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
            }.sumOf { it.originPrice }
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
                .sumOf { it.originPrice }
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
    private fun getProductListFromSource(): List<ProductModel> {
        return listOf(
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2024-04-30T10:23:45"
            ),
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
            ),
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과3",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-05-15T10:23:45"
            ),
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과3",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-05-15T10:23:45"
            ),
            ProductModel(
                productId = 1L,
                listId = 101L,
                name = "사과3",
                originPrice = 1500.0,
                deletedYn = false,
                createdAt = "2025-05-15T10:23:45"
            ),
        )
    }
}