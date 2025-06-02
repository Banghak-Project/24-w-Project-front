package com.example.moneychanger.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.adapter.CalendarDateAdapter
import com.example.moneychanger.adapter.CurrencyBreakdown
import com.example.moneychanger.adapter.FromCurrencyAdapter
import com.example.moneychanger.adapter.HistoryAdapter
import com.example.moneychanger.databinding.FragmentCallendarBinding
import com.example.moneychanger.etc.ExchangeRateUtil
import com.example.moneychanger.home.MainFragment
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.ListWithProductsDto
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.UserInfoResponse
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {
    private var _binding: FragmentCallendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var fromCurrencyAdapter: FromCurrencyAdapter

    // (일별) 클릭한 날짜의 리스트/상품 정보
    private var allProducts = emptyList<ProductResponseDto>()
    private var allLists    = emptyList<ListModel>()

    // 사용자 기본 통화
    private var defaultCurrency: CurrencyModel? = null

    // 화면상으로 보고 있는 연/월
    private var currentYear  = 0
    private var currentMonth = 0

    // 지금 선택되어 있는 날짜 (디폴트는 today)
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onResume() {
        super.onResume()
        setCalendar()
        fetchByDate(selectedDate)
        fetchMonthlyTotal()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 오늘 날짜를 기본으로 세팅
        selectedDate = LocalDate.now()
        currentYear  = selectedDate.year
        currentMonth = selectedDate.monthValue

        // 2) RecyclerView(히스토리 목록) 어댑터 준비
        historyAdapter = HistoryAdapter(allProducts, allLists)
        binding.historyList.layoutManager = LinearLayoutManager(requireContext())
        binding.historyList.adapter = historyAdapter

        // 3) 좌/우 화살표 클릭 → 이전/다음 달
        binding.leftArrow .setOnClickListener { prevMonth() }
        binding.rightArrow.setOnClickListener { nextMonth() }

        // 4) 사용자 기본 통화 가져오기 → 달력을 그린 후, 일별/월별 총합 조회
        lifecycleScope.launch {
            val infoResp = RetrofitClient.apiService.getUserInfo()
            if (infoResp.isSuccessful) {
                infoResp.body()?.data?.let { info: UserInfoResponse ->
                    defaultCurrency = CurrencyManager.getById(info.defaultCurrencyId)
                }
            }

            // 달력(그 달 전체) / 일별 내역 / 월별 총합
            setCalendar()
            fetchByDate(selectedDate)
            fetchMonthlyTotal()
        }
    }

    private fun prevMonth() {
        val (y, m) = DateUtils.moveToPreviousMonth(currentYear, currentMonth)
        currentYear  = y
        currentMonth = m

        // 화면 갱신 (달력 + 월 총합)
        setCalendar()
        fetchMonthlyTotal()

        // 날짜를 바꿔도(월 이동) 히스토리 영역은 초기화
        selectedDate = LocalDate.of(currentYear, currentMonth, 1)
        fetchByDate(selectedDate)
    }

    private fun nextMonth() {
        val (y, m) = DateUtils.moveToNextMonth(currentYear, currentMonth)
        currentYear  = y
        currentMonth = m

        setCalendar()
        fetchMonthlyTotal()

        selectedDate = LocalDate.of(currentYear, currentMonth, 1)
        fetchByDate(selectedDate)
    }

    private fun setCalendar() {
        // 1) 해당 연/월의 첫째날, 말일 문자열 생성
        val startOfMonth = LocalDate.of(currentYear, currentMonth, 1).toString()
        val endOfMonth = YearMonth.of(currentYear, currentMonth)
            .atEndOfMonth().toString()

        // 2) “해당 연/월 전체” 리스트 조회
        lifecycleScope.launch {
            val resp = RetrofitClient.apiService.getListsByDate(startOfMonth, endOfMonth)
            if (!resp.isSuccessful) {
                // 에러 시 달력만 그려 주고 맵은 빈 상태로
                drawCalendarWithMap(emptyMap())
                return@launch
            }

            val dtoList = resp.body()?.data ?: emptyList()
            // 3) 전체 DTO 안의 모든 상품을 모아서, LocalDate별 그룹핑 → eachCount()
            val monthlyProducts = dtoList.flatMap { it.products }
            val recordCountMap: Map<LocalDate, Int> = monthlyProducts
                .filter { !it.deletedYn }
                .groupingBy {
                    LocalDate.parse(it.createdAt.substring(0, 10), DateTimeFormatter.ISO_DATE)
                }
                .eachCount()

            drawCalendarWithMap(recordCountMap)
        }
    }

    private fun drawCalendarWithMap(recordCountMap: Map<LocalDate, Int>) {
        val dateList = DateUtils.generateDateList(currentYear, currentMonth)

        binding.date.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.date.adapter = CalendarDateAdapter(
            dateList       = dateList,
            recordCountMap = recordCountMap,
            selectedDate   = selectedDate
        ) { clicked ->
            // 사용자가 날짜를 클릭했을 때
            selectedDate = clicked
            fetchByDate(clicked)
            // 달력(배지)의 선택된 날짜 표시를 반영하기 위해 다시 그려 준다.
            setCalendar()
        }

        // 화면 상단에 “2025 년 05 월” 같은 텍스트 갱신
        binding.calendarYearText .text = currentYear.toString()
        binding.calendarMonthText.text = currentMonth.toString().padStart(2, '0')
    }

    private fun fetchByDate(date: LocalDate) {
        val uid = TokenManager.getUserId().takeIf { it != -1L } ?: return
        val d   = date.toString() // "2025-05-21"

        lifecycleScope.launch {
            val resp = RetrofitClient.apiService.getListsBySingleDate(d)
            if (!resp.isSuccessful) {
                // 에러 나면 빈 데이터로 처리
                allProducts = emptyList()
                allLists = emptyList()
            } else {
                val dtoList = resp.body()?.data ?: emptyList()
                // 1) allProducts: 실제 상품 DTO들
                allProducts = dtoList.flatMap { it.products }
                    .filter { !it.deletedYn }

                // 2) allLists: ListModel 형태로 바꿔 줌
                allLists = dtoList.map { dto ->
                    ListModel(
                        listId       = dto.listId,
                        name         = dto.name,
                        userId       = uid,
                        createdAt    = dto.createdAt,
                        location     = dto.location,
                        currencyFrom = CurrencyManager.getById(dto.currencyFromId)
                            ?: error("unknown currency id=${dto.currencyFromId}"),
                        currencyTo   = CurrencyManager.getById(dto.currencyToId)
                            ?: error("unknown currency id=${dto.currencyToId}"),
                        deletedYn    = false
                    )
                }
            }

            // 3) “선택된 하루”의 총합 계산 → totalSpendTextView
            val total = allProducts.sumOf { it.originPrice * it.quantity }
            //binding.totalSpendTextView.text = "%,d".format(total.toInt())

            // 4) “환산통화”가 있으면 → 환산액 갱신
            if (defaultCurrency != null) {
                if (allLists.isNotEmpty()) {
                    // - 해당 날짜에 내역이 있으면 → 원본 통화 ID를 가져와서 계산
                    val originCurId = allLists.first().currencyFrom.currencyId
                    val converted = ExchangeRateUtil.calculateExchangeRate(
                        fromId = originCurId,
                        toId   = defaultCurrency!!.currencyId,
                        amount = total.toDouble()
                    )
                    binding.convertedTotalTextView.text    = "%,.2f".format(converted)
                    binding.convertedCurrencyTextView.text = getCurrencySymbol(defaultCurrency!!.curUnit)
                } else {
                    // - 해당 날짜에 내역이 없으면 → “0.00”으로 초기화
                    binding.convertedTotalTextView.text    = "0.00"
                    binding.convertedCurrencyTextView.text =
                        getCurrencySymbol(defaultCurrency!!.curUnit)
                }
            }

            // 5) 히스토리 헤더의 “MM”,”DD” 부분 갱신
            binding.historyMonthText.text = date.monthValue.toString().padStart(2, '0')
            binding.historyDateText .text = date.dayOfMonth       .toString().padStart(2, '0')

            // 6) 어댑터에 무조건 updateList(빈 리스트도 넘겨줘야 “히스토리 없음” 상태로 갱신됨)
            historyAdapter.updateList(allProducts, allLists)

            val productCurrencyMap = allLists.associateBy { it.listId }

            val grouped = allProducts
                .filter { !it.deletedYn }
                .groupBy { product -> productCurrencyMap[product.listId]?.currencyFrom?.currencyId }
                .filterKeys { it != null } // currencyId가 null이 아닌 경우만

            val breakdownList = grouped.map { (currencyId, products) ->
                val sum = products.sumOf { it.originPrice * it.quantity }
                val unit = CurrencyManager.getById(currencyId!!)!!.curUnit
                CurrencyBreakdown(unit, sum)
            }
            fromCurrencyAdapter = FromCurrencyAdapter(breakdownList)
            binding.fromCurrencyList.layoutManager = LinearLayoutManager(requireContext())
            binding.fromCurrencyList.adapter = fromCurrencyAdapter

        }
    }

    private fun fetchMonthlyTotal() {
        val uid = TokenManager.getUserId().takeIf { it != -1L } ?: return
        val start = LocalDate.of(currentYear, currentMonth, 1).toString()
        val end   = YearMonth.of(currentYear, currentMonth).atEndOfMonth().toString()

        lifecycleScope.launch {
            val resp = RetrofitClient.apiService.getListsByDate(start, end)
            if (!resp.isSuccessful) {
                // 에러 시 “0”으로 초기화
                binding.monthTotalTextView.text = "0"
                binding.monthCurrencyTextView.text = ""
                return@launch
            }

            val dtoList    = resp.body()?.data ?: emptyList()
            val monthTotal = dtoList.flatMap { it.products }
                .filter { !it.deletedYn }
                .sumOf { it.originPrice * it.quantity }

            // 1) 원화 표시
            binding.monthTotalTextView.text = "%,d".format(monthTotal.toInt())

            // 2) 환산액 표시
            defaultCurrency?.let { targetCur ->
                if (dtoList.isNotEmpty()) {
                    val originCurId = dtoList.first().currencyFromId
                    val convertedMonth = ExchangeRateUtil.calculateExchangeRate(
                        fromId = originCurId,
                        toId   = targetCur.currencyId,
                        amount = monthTotal.toDouble()
                    )
                    binding.monthTotalTextView.text    = "%,.2f".format(convertedMonth)
                    binding.monthCurrencyTextView.text = getCurrencySymbol(targetCur.curUnit)
                } else {
                    binding.monthTotalTextView.text    = "0.00"
                    binding.monthCurrencyTextView.text = getCurrencySymbol(targetCur.curUnit)
                }
            }
        }
    }

    private fun getCurrencySymbol(code: String): String {
        val key = code.replace(Regex("\\(.*\\)"), "").trim()
        val resId = resources.getIdentifier(key, "string", requireContext().packageName)
        return if (resId != 0) getString(resId) else key
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
