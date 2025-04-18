package com.example.moneychanger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListPlaceBinding
import com.example.moneychanger.etc.ExchangeRateUtil
import com.example.moneychanger.etc.ExchangeRateUtil.calculateExchangeRate
import com.example.moneychanger.etc.TotalAmountUtil
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.user.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency

class ListAdapter(
    private val items: MutableList<ListModel>,
    private val onItemClick: (ListModel) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    val apiService = RetrofitClient.apiService
    private var isDeleteMode = false

    inner class ListViewHolder(val binding: ListPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        //val imageViewDelete: ImageView = view.findViewById(R.id.imageViewDelete)
        fun bind(item: ListModel) {
            binding.locationName.isSelected = true
            binding.placeName.isSelected = true
            binding.grossPayment.isSelected = true
            binding.grossPaymentConverted.isSelected = true

            binding.placeName.text = item.name
            binding.locationName.text = item.location
            fetchTotalAmount(item.listId, item.currencyTo, item.currencyFrom, binding)

            val context = binding.root.context
            val fromKey = item.currencyFrom.toString().replace(Regex("\\(.*\\)"), "").trim()
            val toKey = item.currencyTo.toString().replace(Regex("\\(.*\\)"), "").trim()
            val fromResId = context.resources.getIdentifier(fromKey, "string", context.packageName)
            val toResId = context.resources.getIdentifier(toKey, "string", context.packageName)
            val fromSymbol = if (fromResId != 0) context.getString(fromResId) else fromKey
            val toSymbol = if (toResId != 0) context.getString(toResId) else toKey
            binding.currencyFrom.text = fromSymbol
            binding.currencyTo.text = toSymbol

            val dateTime = LocalDateTime.parse(item.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.createdDate.text = dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분"))

            itemView.setOnClickListener {
                if (!isDeleteMode) onItemClick(item) // 삭제 모드가 아닐 때만 클릭 가능
            }
            // 삭제 버튼 가시성 조정
            binding.deleteIcon.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            binding.dateBox.visibility = if (!isDeleteMode) View.VISIBLE else View.GONE

            // 삭제 아이콘 클릭 시 해당 아이템 삭제
            binding.deleteIcon.setOnClickListener {
                if (isDeleteMode && adapterPosition != RecyclerView.NO_POSITION) {
                    deleteItem(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun toggledeleteMode() {
        isDeleteMode = !isDeleteMode
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        if (position in items.indices) {
            val listId = items[position].listId

            apiService.deleteList(listId).enqueue(object : Callback<ApiResponse<Void>> {
                override fun onResponse(
                    call: Call<ApiResponse<Void>>,
                    response: Response<ApiResponse<Void>>
                ) {
                    if (response.isSuccessful) {
                        // 서버에서 삭제 성공한 경우, UI에서도 삭제
                        items.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, items.size)
                    } else {
                        // 서버에서 삭제 실패한 경우 로그 출력
                        Log.e("DeleteItem", "삭제 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                    // 네트워크 요청 실패 시 로그 출력
                    Log.e("DeleteItem", "API 호출 실패", t)
                }
            })
        }
    }

    fun fetchTotalAmount(listId: Long, currencyFrom: CurrencyModel, currencyTo: CurrencyModel, binding: ListPlaceBinding) {
        TotalAmountUtil.fetchTotalAmount(listId) { total ->
            binding.grossPayment.text = String.format("%.2f", total)

            // 비동기 환율 적용
            CoroutineScope(Dispatchers.Main).launch {
                val converted = calculateExchangeRate(
                    currencyTo.currencyId,
                    currencyFrom.currencyId,
                    total
                )
                binding.grossPaymentConverted.text = String.format("%.2f", converted)
            }
        }
    }


    fun addItem(item: ListModel) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
    fun updateList(newList: List<ListModel>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].listId == newList[newItemPosition].listId
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newList[newItemPosition]
            }
        }
        Log.d("ListAdapter", "updateData 호출됨: ${newList.size}개")
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

}

