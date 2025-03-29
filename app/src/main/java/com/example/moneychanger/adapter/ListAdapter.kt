package com.example.moneychanger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListPlaceBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.user.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListAdapter(
    private val items: MutableList<ListModel>,
    private val onItemClick: (ListModel) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    val apiService = RetrofitClient.apiService
    private var isDeleteMode = false

    inner class ListViewHolder(val binding: ListPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        //val imageViewDelete: ImageView = view.findViewById(R.id.imageViewDelete)
        fun bind(item: ListModel) {
            binding.placeName.text = item.name
            binding.locationName.text = item.location
            fetchTotalAmount(item.listId, binding)

            val dateTime = LocalDateTime.parse(item.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.createdDate.text = dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분 ss초"))

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

            apiService.deleteList(listId).enqueue(object : retrofit2.Callback<ApiResponse<Void>> {
                override fun onResponse(
                    call: retrofit2.Call<ApiResponse<Void>>,
                    response: retrofit2.Response<ApiResponse<Void>>
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

                override fun onFailure(call: retrofit2.Call<ApiResponse<Void>>, t: Throwable) {
                    // 네트워크 요청 실패 시 로그 출력
                    Log.e("DeleteItem", "API 호출 실패", t)
                }
            })
        }
    }

    private fun fetchTotalAmount(listId: Long, binding: ListPlaceBinding) {
        apiService.getTotal(listId).enqueue(object : Callback<ApiResponse<Double>> {
            override fun onResponse(
                call: Call<ApiResponse<Double>>,
                response: Response<ApiResponse<Double>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val totalAmount = response.body()?.data ?: 0.0
                    binding.grossPayment.text = String.format("%.2f", totalAmount)
                } else {
                    Log.e("ListAdapter", "총 금액 응답 실패: ${response.errorBody()?.string()}")
                    binding.grossPayment.text = "0.0"
                }
            }
            override fun onFailure(call: Call<ApiResponse<Double>>, t: Throwable) {
                Log.e("ListAdapter", "총 금액 API 호출 실패", t)
                binding.grossPayment.text = "0.0"
            }
        })
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

