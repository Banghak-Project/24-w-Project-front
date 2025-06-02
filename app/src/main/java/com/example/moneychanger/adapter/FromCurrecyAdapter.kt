package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListFromCurrencyBinding

class FromCurrencyAdapter(
    private val items: List<CurrencyBreakdown>
) : RecyclerView.Adapter<FromCurrencyAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ListFromCurrencyBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CurrencyBreakdown) {
            val context = binding.root.context

            val currencyToUnit = item.currencyUnit
            val toKey = currencyToUnit.replace(Regex("\\(.*\\)"), "").trim()
            val toResId = context.resources.getIdentifier(toKey, "string", context.packageName)
            val toSymbol = if (toResId != 0) context.getString(toResId) else toKey

            binding.currencySymbol.text = toSymbol
            binding.totalText.text = item.amount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListFromCurrencyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

data class CurrencyBreakdown(
    val currencyUnit: String,
    val amount: Double
)
