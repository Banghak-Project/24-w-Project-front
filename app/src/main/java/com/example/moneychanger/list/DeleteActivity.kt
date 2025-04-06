package com.example.moneychanger.list

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.R
import com.example.moneychanger.adapter.DeleteAdapter
import com.example.moneychanger.databinding.ActivityDeleteBinding
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeleteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteBinding
    private lateinit var adapter: DeleteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        binding.loginToolbar.pageText.text = "삭제하기"

        // listActivity에서 전달받은 list_id
        val selectedListId = intent.getLongExtra("list_id", 0L)
        if (selectedListId != 0L) {
            fetchProductList(selectedListId)
        } else {
            Toast.makeText(this, "리스트 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 삭제 버튼 이벤트 (화면에서만 상품 삭제)
        binding.buttonDelete.setOnClickListener {
            adapter.deleteSelectedItems()
            binding.checkboxAll.isChecked = false
        }

        // 전체 선택 체크박스 클릭 시
        binding.checkboxAll.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAllItems(isChecked)
        }
    }
        private fun fetchProductList(listId: Long) {
            apiService.getProductByListsId(listId)
                .enqueue(object : Callback<ApiResponse<List<ProductResponseDto>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<List<ProductResponseDto>>>,
                        response: Response<ApiResponse<List<ProductResponseDto>>>
                    ) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse != null && apiResponse.status == "success") {
                                val productList = apiResponse.data ?: emptyList()
                                val mappedList = productList.map { mapToProductModel(it) }.toMutableList()
                                setupRecyclerView(mappedList)

                            } else {
                                Toast.makeText(this@DeleteActivity, "상품 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("DeleteActivity", "🚨 상품 목록 응답 실패: ${response.errorBody()?.string()}")
                            Toast.makeText(this@DeleteActivity, "상품 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProductResponseDto>>>, t: Throwable) {
                        Log.e("DeleteActivity", "🚨 상품 목록 서버 요청 실패: ${t.message}")
                        Toast.makeText(this@DeleteActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    private fun setupRecyclerView(productResponseList: MutableList<ProductModel>) {
        val productList = productResponseList.toMutableList()
        adapter = DeleteAdapter(productList, { selectedItems ->
            fetchProductList(intent.getLongExtra("list_id", 0L))
        }, { isChecked ->
            binding.checkboxAll.isChecked = isChecked
        })


        binding.deleteContainer.layoutManager = LinearLayoutManager(this)
        binding.deleteContainer.adapter = adapter
    }

    private fun mapToProductModel(dto: ProductResponseDto): ProductModel {
        return ProductModel(
            productId = dto.productId,
            listId = dto.listId,
            name = dto.name,
            originPrice = dto.originPrice,
            createdAt = dto.createdAt,
            deletedYn = dto.deletedYn
        )
    }
    private fun deleteProductsFromDB(productIds: List<Long>) {
        for (productId in productIds) {
            apiService.deleteProduct(productId)
                .enqueue(object : Callback<ApiResponse<Void>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Void>>,
                        response: Response<ApiResponse<Void>>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Log.d("DeleteActivity", "✅ 상품 삭제 성공: $productId")
                        } else {
                            Log.e("DeleteActivity", "🚨 상품 삭제 실패: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                        Log.e("DeleteActivity", "🚨 서버 요청 실패: ${t.message}")
                    }
                })
        }
    }
}
