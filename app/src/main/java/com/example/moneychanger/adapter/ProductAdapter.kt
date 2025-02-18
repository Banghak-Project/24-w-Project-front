package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ListProductBinding
import com.example.moneychanger.network.product.ProductModel

class ProductAdapter(
    private val products: MutableList<ProductModel>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ListProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductModel, position: Int, totalCount: Int) {
            binding.productName.text = product.name
            binding.originPrice.text = product.originPrice.toString()
            binding.perNumber.text = (position + 1).toString()
            binding.allNumber.text = totalCount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ListProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position], position, products.size)
    }

    override fun getItemCount(): Int = products.size

    fun deleteItem(position: Int) {
        products.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, products.size)
    }

    fun updateProducts(newProducts: List<ProductModel>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

}


