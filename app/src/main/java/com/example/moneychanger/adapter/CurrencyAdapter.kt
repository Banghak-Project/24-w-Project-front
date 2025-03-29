package com.example.moneychanger.adapter

//import androidx.recyclerview.widget.RecyclerView

// 차후 list_product에 사용할수도 있는 임시 adapter 코드.
//class CurrencyAdapter(private val viewModel: CurrencyViewModel) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {
//
//    private val items = mutableListOf<String>()
//
//    inner class CurrencyViewHolder(val binding: ItemCurrencyBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: String) {
//            binding.itemText.text = item // 기본 데이터 설정
//
//            // LiveData 관찰하여 기호 업데이트
//            viewModel.selectedCurrency.observeForever { selectedCurrency ->
//                val resourceId = binding.root.context.resources.getIdentifier(selectedCurrency, "string", binding.root.context.packageName)
//                binding.currencySymbol.text = if (resourceId != 0) binding.root.context.getString(resourceId) else selectedCurrency
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding = ItemCurrencyBinding.inflate(inflater, parent, false)
//        return CurrencyViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
//        holder.bind(items[position])
//    }
//
//    override fun getItemCount() = items.size
//
//    fun updateItems(newItems: List<String>) {
//        items.clear()
//        items.addAll(newItems)
//        notifyDataSetChanged()
//    }
//}
