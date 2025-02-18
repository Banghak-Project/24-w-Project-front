package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ListDeleteBinding
import com.example.moneychanger.databinding.ListPlaceBinding
import com.example.moneychanger.network.list.ListModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListAdapter(
    private val items: MutableList<ListModel>,
    private val onItemClick: (ListModel) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    inner class ListViewHolder(val binding: ListPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        //val imageViewDelete: ImageView = view.findViewById(R.id.imageViewDelete)
        fun bind(item: ListModel) {
            binding.placeName.text = item.name
            binding.locationName.text = item.location

            val dateTime = LocalDateTime.parse(item.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.createdDate.text = dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            binding.createdTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            itemView.setOnClickListener { onItemClick(item) }
            //imageViewDelete.setOnClickListener { deleteItem(adapterPosition) }
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

    fun deleteItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
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
}

