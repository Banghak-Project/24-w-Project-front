package com.example.moneychanger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListHistoryBinding
import com.example.moneychanger.etc.ExchangeRateUtil
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.product.ProductResponseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryAdapter(
    private var productList: List<ProductResponseDto>,
    private var listList: List<ListModel>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ListHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductResponseDto, list: ListModel) {
            // 1) 장소명 / 시간 / 상품명
            binding.placeName.text = list.name
            val dateTime = try {
                LocalDateTime.parse(product.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                Log.e("HistoryAdapter", "createdAt 파싱 실패: ${product.createdAt}", e)
                LocalDateTime.now()
            }
            binding.productTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분"))
            binding.productName.text = product.name

            // 2) 원래 금액
            binding.productPrice.text = String.format("%,d", product.originPrice.toInt())

            // —————————————————————————————————————————————
            // 3) curUnit 기반 리소스 조회해서 심볼 표시
            //
            //    list.currencyTo.curUnit  = ex) "USD" or "JPY(100)"
            //    list.currencyFrom.curUnit = ex) "KRW"
            //
            val ctx = binding.root.context
            val toKey   = list.currencyTo.curUnit.replace(Regex("\\(.*\\)"), "").trim()
            val fromKey = list.currencyFrom.curUnit.replace(Regex("\\(.*\\)"), "").trim()

            val toResId   = ctx.resources.getIdentifier(toKey,   "string", ctx.packageName)
            val fromResId = ctx.resources.getIdentifier(fromKey, "string", ctx.packageName)

            val toSymbol   = if (toResId   != 0) ctx.getString(toResId)   else toKey
            val fromSymbol = if (fromResId != 0) ctx.getString(fromResId) else fromKey

            binding.currencyTo.text   = toSymbol   // 원래 금액 통화 로고
            binding.currencyFrom.text = fromSymbol // 변환 후 통화 로고
            // —————————————————————————————————————————————

            // 4) 환율 변환 → original 통화(list.currencyTo) → 목표 통화(list.currencyFrom)
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("HistConv",
                    "from=${list.currencyTo.currencyId}($toSymbol) " +
                            "to=${list.currencyFrom.currencyId}($fromSymbol) " +
                            "amount=${product.originPrice}"
                )
                val converted = ExchangeRateUtil.calculateExchangeRate(
                    list.currencyTo.currencyId,    // from: 원래 금액 통화
                    list.currencyFrom.currencyId,  // to: 변환할 통화
                    product.originPrice.toDouble()
                )
                Log.d("HistConv", "converted=$converted")
                binding.convertedPrice.text = String.format("%,.2f", converted)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ListHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val product = productList[position]
        val list    = listList.find { it.listId == product.listId }
        if (list != null) {
            holder.bind(product, list)
        } else {
            Log.e("HistoryAdapter", "ListModel 누락: listId=${product.listId}")
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(
        newProductList: List<ProductResponseDto>,
        newListList:      List<ListModel>
    ) {
        productList = newProductList
        listList    = newListList
        notifyDataSetChanged()
    }
}
