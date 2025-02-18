package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.network.product.ProductModel

class ProductAdapter(private val products: MutableList<ProductModel>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val originalPrice: TextView = itemView.findViewById(R.id.origin_price)
        private val perNumber: TextView = itemView.findViewById(R.id.per_number)
        private val allNumber: TextView = itemView.findViewById(R.id.all_number)

        fun bind(product: ProductModel, position: Int, totalCount: Int) {
            productName.text = product.name
            originalPrice.text = product.originPrice.toString()
            perNumber.text = (position + 1).toString()
            allNumber.text = totalCount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_product, parent, false)
        return ProductViewHolder(view)
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


