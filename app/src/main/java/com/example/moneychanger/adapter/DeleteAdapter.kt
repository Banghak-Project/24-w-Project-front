package com.example.moneychanger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ListDeleteBinding
import com.example.moneychanger.list.DeleteActivity
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.product.DeleteProductsRequestDto
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import retrofit2.Call

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
        val selectedList = selectedItems.toList()
        Log.d("DeleteAdapter", "선택된 제품 리스트: $selectedList")
        val selectedIds = selectedList.map { it.productId }
        Log.d("DeleteAdapter", "삭제할 productId 목록: $selectedIds")

        val requestDto = DeleteProductsRequestDto(productIds = selectedIds)

        apiService.deleteProductsByIds(requestDto).enqueue(object : retrofit2.Callback<ApiResponse<List<ProductResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<ProductResponseDto>>>,
                response: retrofit2.Response<ApiResponse<List<ProductResponseDto>>>
            ) {
                if (response.isSuccessful) {
                    val iterator = productList.iterator()
                    while (iterator.hasNext()) {
                        if (iterator.next().productId in selectedIds) {
                            iterator.remove()
                        }
                    }

                    selectedItems.clear()
                    notifyDataSetChanged()
                    Log.d("DeleteAdapter", "삭제완료")
                    Log.d("DeleteAdapter", "${productList.size}")

                } else {
                    // 실패 처리
                    println("삭제 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<ProductResponseDto>>>, t: Throwable) {
                // 네트워크 실패 처리
                println("네트워크 오류: ${t.message}")
            }
        })
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