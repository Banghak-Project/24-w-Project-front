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
import com.example.moneychanger.adapter.HistoryAdapter
import com.example.moneychanger.databinding.FragmentCallendarBinding
import com.example.moneychanger.etc.ExchangeRateUtil
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

    private var allProducts = emptyList<ProductResponseDto>()
    private var allLists    = emptyList<ListModel>()
    private var defaultCurrency: CurrencyModel? = null

    private var currentYear  = 0
    private var currentMonth = 0
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedDate = LocalDate.now()
        currentYear  = selectedDate.year
        currentMonth = selectedDate.monthValue

        historyAdapter = HistoryAdapter(allProducts, allLists)
        binding.historyList.layoutManager = LinearLayoutManager(requireContext())
        binding.historyList.adapter        = historyAdapter

        binding.leftArrow .setOnClickListener { prevMonth() }
        binding.rightArrow.setOnClickListener { nextMonth() }

        lifecycleScope.launch {
            RetrofitClient.apiService.getUserInfo().takeIf { it.isSuccessful }?.body()?.data
                ?.let { info: UserInfoResponse ->
                    defaultCurrency = CurrencyManager.getById(info.defaultCurrencyId)
                }
            setCalendar()
            fetchByDate(selectedDate)
            fetchMonthlyTotal()
        }
    }

    private fun prevMonth() {
        val (y, m) = DateUtils.moveToPreviousMonth(currentYear, currentMonth)
        currentYear  = y
        currentMonth = m
        setCalendar()
        fetchMonthlyTotal()
    }

    private fun nextMonth() {
        val (y, m) = DateUtils.moveToNextMonth(currentYear, currentMonth)
        currentYear  = y
        currentMonth = m
        setCalendar()
        fetchMonthlyTotal()
    }

    private fun setCalendar() {
        val dateList = DateUtils.generateDateList(currentYear, currentMonth)
        val recordCountMap = allProducts
            .filter { !it.deletedYn }
            .groupingBy {
                LocalDate.parse(it.createdAt.substring(0, 10), DateTimeFormatter.ISO_DATE)
            }
            .eachCount()

        binding.date.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.date.adapter = CalendarDateAdapter(
            dateList       = dateList,
            recordCountMap = recordCountMap,
            selectedDate   = selectedDate
        ) { clicked ->
            selectedDate = clicked
            fetchByDate(clicked)
            setCalendar()
        }

        binding.calendarYearText .text = currentYear.toString()
        binding.calendarMonthText.text = currentMonth.toString().padStart(2, '0')
    }

    private fun fetchByDate(date: LocalDate) {
        val uid = TokenManager.getUserId().takeIf { it != -1L } ?: return
        val d   = date.toString()

        lifecycleScope.launch {
            val resp = RetrofitClient.apiService.getListsBySingleDate(d)
            if (!resp.isSuccessful) {
                return@launch
            }

            val dtoList = resp.body()?.data ?: emptyList<ListWithProductsDto>()
            allProducts = dtoList.flatMap { it.products }
            allLists    = dtoList.map { dto ->
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

            val total = allProducts.sumOf { it.originPrice*it.quantity }
            binding.totalSpendTextView.text = "%,d".format(total.toInt())

            defaultCurrency?.let { targetCur ->
                val originCurId = allLists.firstOrNull()?.currencyFrom?.currencyId ?: return@let
                val converted = ExchangeRateUtil.calculateExchangeRate(
                    fromId = originCurId,
                    toId   = targetCur.currencyId,
                    amount = total.toDouble()
                )
                binding.convertedTotalTextView.text    = "%,.2f".format(converted)
                binding.convertedCurrencyTextView.text = getCurrencySymbol(targetCur.curUnit)
            }

            binding.historyMonthText.text = date.monthValue.toString().padStart(2, '0')
            binding.historyDateText .text = date.dayOfMonth        .toString().padStart(2, '0')

            historyAdapter.updateList(allProducts, allLists)
        }
    }

    private fun fetchMonthlyTotal() {
        val uid = TokenManager.getUserId().takeIf { it != -1L } ?: return
        val start = LocalDate.of(currentYear, currentMonth, 1).toString()
        val end   = YearMonth.of(currentYear, currentMonth).atEndOfMonth().toString()

        lifecycleScope.launch {
            val resp = RetrofitClient.apiService.getListsByDate(start, end)
            if (!resp.isSuccessful) {
                return@launch
            }

            val dtoList    = resp.body()?.data ?: emptyList<ListWithProductsDto>()
            val monthTotal = dtoList.flatMap { it.products }
                .sumOf { it.originPrice*it.quantity }
            binding.monthTotalTextView.text = "%,d".format(monthTotal.toInt())

            defaultCurrency?.let { targetCur ->
                val originCurId = dtoList.firstOrNull()?.currencyFromId ?: return@let
                val convertedMonth = ExchangeRateUtil.calculateExchangeRate(
                    fromId = originCurId,
                    toId   = targetCur.currencyId,
                    amount = monthTotal.toDouble()
                )
                binding.monthTotalTextView.text    = "%,.2f".format(convertedMonth)
                binding.monthCurrencyTextView.text = getCurrencySymbol(targetCur.curUnit)
            }
        }
    }

    private fun getCurrencySymbol(code: String): String {
        val key = code.replace(Regex("\\(.*\\)"), "").trim()
        val resId = resources.getIdentifier(key, "string", requireContext().packageName)
        return if (resId != 0) getString(resId) else key
    }
}
