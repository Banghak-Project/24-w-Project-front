package com.example.moneychanger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.CalendarDateTileBinding
import com.example.moneychanger.databinding.ListHistoryBinding
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.product.ProductModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryAdapter(
    private var productList: List<ProductModel>,
    private var listList: List<ListModel>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ListHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductModel, list: ListModel) {
            binding.placeName.text = list.name
            val dateTime = try {
                LocalDateTime.parse(product.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                Log.e("HistoryAdapter", "createdAt 파싱 실패: ${product.createdAt}", e)
                LocalDateTime.now()
            }
            binding.productTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분"))

            binding.productName.text = product.name
            binding.productPrice.text = product.originPrice.toString()

            val context = binding.root.context
            val fromKey = list.currencyFrom.curUnit.replace(Regex("\\(.*\\)"), "").trim()
            val toKey = list.currencyTo.curUnit.replace(Regex("\\(.*\\)"), "").trim()
            val fromResId = context.resources.getIdentifier(fromKey, "string", context.packageName)
            val toResId = context.resources.getIdentifier(toKey, "string", context.packageName)
            val fromSymbol = if (fromResId != 0) context.getString(fromResId) else fromKey
            val toSymbol = if (toResId != 0) context.getString(toResId) else toKey
            binding.currencyFrom.text = fromSymbol
            binding.currencyTo.text = toSymbol
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ListHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val product = productList[position]
        val list = listList.find { it.listId == product.listId }

        if (list != null) {
            holder.bind(product, list)
        } else {
            Log.e("HistoryAdapter", "ListModel not found for listId=${product.listId}")
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<ProductModel>, newListList: List<ListModel>) {
        productList = newList
        listList = newListList
        notifyDataSetChanged()
    }

}
