package com.example.moneychanger.etc

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moneychanger.databinding.SlideProductEditBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.product.UpdateProductRequestDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SlideProductEdit : BottomSheetDialogFragment() {

    private lateinit var binding: SlideProductEditBinding
    private var product: ProductModel? = null

    private var onProductUpdatedListener: ((ProductModel) -> Unit)? = null

    fun setOnProductUpdatedListener(listener: (ProductModel) -> Unit) {
        onProductUpdatedListener = listener
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED // 슬라이드 최대 크기로 시작

            val layoutParams = it.layoutParams
            layoutParams.height = dpToPx(368f).toInt() // 전체 높이 설정
            it.layoutParams = layoutParams
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = arguments?.getParcelable("product")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SlideProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 통화 기호 바인딩
        val currencyToUnit = arguments?.getString("currency_from_unit") ?: ""
        val toKey = currencyToUnit.replace(Regex("\\(.*\\)"), "").trim()
        val toResId = resources.getIdentifier(toKey, "string", requireContext().packageName)
        val toSymbol = if (toResId != 0) getString(toResId) else toKey

        binding.currencyText.text = currencyToUnit
        binding.currencySymbol.text = toSymbol

        // 기존 정보 표시
        product?.let {
            binding.inputName.setText(it.name)
            binding.inputPrice.setText(it.originPrice.toString())
            binding.countText.setText(it.quantity.toString())
        }

        // 확인 버튼 클릭 시
        binding.buttonAdd.setOnClickListener {
            val newName = binding.inputName.text.toString()
            val newPriceStr = binding.inputPrice.text.toString()
            val quantity = binding.countText.text.toString().toInt()

            if (newName.isBlank() || newPriceStr.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPrice = newPriceStr.toDoubleOrNull()
            if (newPrice == null) {
                Toast.makeText(requireContext(), "가격을 숫자로 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val productId = product?.productId ?: return@setOnClickListener
            val updateRequest = UpdateProductRequestDto(productId, newName, quantity, newPrice)

            updateListCurrency(updateRequest)
        }

    }

    private fun updateListCurrency(updateRequest: UpdateProductRequestDto) {
        RetrofitClient.apiService.updateProduct(updateRequest)
        .enqueue(object : Callback<ApiResponse<ProductResponseDto>> {
            override fun onResponse(
                call: Call<ApiResponse<ProductResponseDto>>,
                response: Response<ApiResponse<ProductResponseDto>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(requireContext(), "상품 수정 완료", Toast.LENGTH_SHORT).show()
                    val updatedProduct = response.body()?.data
                    Log.d("ProductEdit", "업데이트: $updatedProduct")
                    val productsModel = updatedProduct?.let {
                        ProductModel(
                            productId = it.productId,
                            listId = updatedProduct.listId,
                            name = updatedProduct.name,
                            quantity = updatedProduct.quantity,
                            originPrice = updatedProduct.originPrice,
                            deletedYn = updatedProduct.deletedYn,
                            createdAt = updatedProduct.createdAt
                        )
                    }
                    if (updatedProduct != null) {
                        if (productsModel != null) {
                            onProductUpdatedListener?.invoke(productsModel)
                        }
                        Log.d("ProductEdit", "업데이트 콜백 호출됨: $productsModel")
                    }
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "상품 수정 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<ProductResponseDto>>, t: Throwable) {
                Toast.makeText(requireContext(), "서버 통신 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        const val TAG = "SlideProductEdit"
    }
}

fun dpToPx(dp: Float): Float {
    return dp * Resources.getSystem().displayMetrics.density
}