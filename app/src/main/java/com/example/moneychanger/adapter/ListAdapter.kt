package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListPlaceBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.list.ListModel
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

            val dateTime = LocalDateTime.parse(item.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.createdDate.text = dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

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
            apiService.deleteList(listId)
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
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

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateData(newList: List<ListModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged() // UI 갱신
    }
}

