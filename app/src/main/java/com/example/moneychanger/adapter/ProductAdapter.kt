package com.example.moneychanger.adapter

import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.databinding.ListProductBinding
import com.example.moneychanger.etc.ExchangeRateUtil
import com.example.moneychanger.etc.ExchangeRateUtil.calculateExchangeRate
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.network.product.CreateProductResponseDto
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProductAdapter(
    private var products: MutableList<ProductModel>,
    private val currencyIdFrom: Long,
    private val currencyIdTo: Long,
    private val currencyFromUnit: String,
    private val currencyToUnit: String,
    private val editListener: ListActivity.OnProductEditListener,
    private val showEditButton: Boolean = true
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ListProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductModel, position: Int, totalCount: Int) {
            binding.productName.isSelected = true

            Log.i("ProductAdapter",product.originPrice.toString())
            binding.productName.text = product.name
            binding.originPrice.text = product.originPrice.toString()
            val dateTime = try {
                LocalDateTime.parse(product.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                Log.e("ProductAdapter", "createdAt 파싱 실패: ${product.createdAt}", e)
                LocalDateTime.now()
            }
            //val dateTime = LocalDateTime.parse(product.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.productTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH시 mm분"))

            val context = binding.root.context
            val fromKey = currencyFromUnit.replace(Regex("\\(.*\\)"), "").trim()
            val toKey = currencyToUnit.replace(Regex("\\(.*\\)"), "").trim()
            val fromResId = context.resources.getIdentifier(fromKey, "string", context.packageName)
            val toResId = context.resources.getIdentifier(toKey, "string", context.packageName)
            val fromSymbol = if (fromResId != 0) context.getString(fromResId) else fromKey
            val toSymbol = if (toResId != 0) context.getString(toResId) else toKey
            binding.currencyFrom.text = fromSymbol
            binding.currencyTo.text = toSymbol

            CoroutineScope(Dispatchers.Main).launch {
                val converted = calculateExchangeRate(currencyIdFrom, currencyIdTo, product.originPrice ?: 0.0)
                binding.convertedPrice.text = String.format("%.2f", converted)
            }

            binding.perNumber.text = (position + 1).toString()
            binding.allNumber.text = totalCount.toString()

            binding.buttonEdit.setOnClickListener{
                editListener.onEditRequested(product)
            }
            binding.buttonEdit.visibility = if(showEditButton) View.VISIBLE else View.INVISIBLE
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

    fun updateItem(updatedProduct: ProductModel) {
        val index = products.indexOfFirst { it.productId == updatedProduct.productId }
        if (index != -1) {
            products[index] = updatedProduct
            notifyDataSetChanged()
        }
    }

    fun updateList(newList: MutableList<ProductModel>) {
        products = newList
        notifyDataSetChanged()
    }

    fun updateListCamera(newList: MutableList<ProductModel>) {
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }


}


