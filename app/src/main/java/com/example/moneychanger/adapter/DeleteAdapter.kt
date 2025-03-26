package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ListDeleteBinding
import com.example.moneychanger.list.DeleteActivity
import com.example.moneychanger.network.product.ProductModel

class DeleteAdapter(
    private val productList: MutableList<ProductModel>,
    private val onDeleteSelected: (List<ProductModel>) -> Unit,
    private val onItemCheckedChange: (Boolean) -> Unit
) : RecyclerView.Adapter<DeleteAdapter.DeleteViewHolder>() {

    private val selectedItems = mutableSetOf<ProductModel>()

    inner class DeleteViewHolder(val binding: ListDeleteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductModel, position: Int) {
            binding.productName.text = product.name
            binding.perNumber.text = "${position + 1}"
            binding.allNumber.text = "${productList.size}"
            binding.checkbox.isChecked = selectedItems.contains(product)

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(product)
                    if (selectedItems.size == productList.size) {
                        onItemCheckedChange(true)  // 모두 선택 시 true 전달
                    }
                } else {
                    selectedItems.remove(product)
                    onItemCheckedChange(false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteViewHolder {
        val binding = ListDeleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeleteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeleteViewHolder, position: Int) {
        holder.bind(productList[position], position)
    }

    override fun getItemCount() = productList.size

    fun deleteSelectedItems() {
        productList.removeAll(selectedItems)
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun selectAllItems(selectAll: Boolean) {
        if (selectAll) {
            selectedItems.clear()
            selectedItems.addAll(productList)
        } else {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
}