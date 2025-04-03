package com.example.moneychanger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListProductBinding
import com.example.moneychanger.etc.ExchangeRateUtil
import com.example.moneychanger.network.product.ProductModel

class ProductAdapter(
    private val products: MutableList<ProductModel>,
    private val currencyIdFrom: Long,
    private val currencyIdTo: Long
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ListProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductModel, position: Int, totalCount: Int) {
            Log.i("ProductAdapter",product.originPrice.toString())
            binding.productName.text = product.name
            binding.originPrice.text = product.originPrice.toString()
            binding.productTime.text = product.createdAt

            val converted = ExchangeRateUtil.calculate(currencyIdFrom, currencyIdTo, product.originPrice)
            binding.convertedPrice.text = String.format("%.2f", converted)

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


