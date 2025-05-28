package com.example.moneychanger.etc

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.databinding.SlideCameraListBinding
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SlideCameraList : BottomSheetDialogFragment() {
    private var _binding: SlideCameraListBinding? = null
    private val binding get() = _binding!!

    private var listener: OnProductAddedListener? = null
    private lateinit var productAdapter: ProductAdapter  // 기존 ProductAdapter 사용

    companion object {
        const val TAG = "SlideCameraList"

        fun newInstance(
            productResponseList: List<ProductResponseDto>,
            currencyIdFrom: Long,
            currencyIdTo: Long,
            currencyFromUnit: String,
            currencyToUnit: String,
            listId: Long
        ): SlideCameraList {
            val fragment = SlideCameraList()
            val args = Bundle()

            val productList = productResponseList.map {
                ProductModel(
                    productId = it.productId,
                    listId = it.listId,
                    name = it.name,
                    quantity = it.quantity,
                    originPrice = it.originPrice,
                    createdAt = it.createdAt,
                    deletedYn = it.deletedYn
                )
            }

            args.putParcelableArrayList("product_list", ArrayList(productList))
            args.putLong("currency_id_from", currencyIdFrom)
            args.putLong("currency_id_to", currencyIdTo)
            args.putString("currency_from_unit", currencyFromUnit)
            args.putString("currency_to_unit", currencyToUnit)
            args.putLong("list_id", listId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProductAddedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnProductAddedListener")
        }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SlideCameraListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productList = arguments?.getParcelableArrayList<ProductModel>("product_list")?.toMutableList() ?: mutableListOf()
        val currencyIdFrom = arguments?.getLong("currency_id_from") ?: -1L
        val currencyIdTo = arguments?.getLong("currency_id_to") ?: -1L
        val currencyFromUnit = arguments?.getString("currency_from_unit") ?: ""
        val currencyToUnit = arguments?.getString("currency_to_unit") ?: ""
        val listId = arguments?.getLong("list_id") ?: -1L

        val editListener = object : ListActivity.OnProductEditListener {
            override fun onEditRequested(product: ProductModel) {
                val slideEdit = SlideProductEdit().apply {
                    arguments = Bundle().apply {
                        putParcelable("product", product)
                        putString("currency_from_unit", currencyFromUnit)
                        putString("currency_to_unit", currencyToUnit)
                    }
                }
                slideEdit.show(parentFragmentManager, "SlideProductEdit")
            }
        }

        productAdapter = ProductAdapter(
            productList,
            currencyIdFrom,
            currencyIdTo,
            currencyFromUnit,
            currencyToUnit,
            editListener,
            showEditButton = false
        )

        binding.productContainer.adapter = productAdapter

        fetchProductsAndUpdate(listId)

        binding.buttonAdd.setOnClickListener {
            val slideCameraInput = SlideCameraInput { newlyAddedProduct ->
                Log.d("SlideCameraList", "새 상품 추가됨: ${newlyAddedProduct.name}")
                fetchProductsAndUpdate(newlyAddedProduct.listId)
            }.apply {
                arguments = Bundle().apply {
                    putString("currency_from_unit", currencyFromUnit)
                    putLong("list_id", listId)
                }
            }
            slideCameraInput.show(parentFragmentManager, SlideCameraInput.TAG)
        }
    }
    private fun fetchProductsAndUpdate(listId: Long ,onComplete: (() -> Unit)?= null) {
        RetrofitClient.apiService.getProductByListsId(listId)
            .enqueue(object : Callback<ApiResponse<List<ProductResponseDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<ProductResponseDto>>>,
                    response: Response<ApiResponse<List<ProductResponseDto>>>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == "success") {
                            val productListDto = apiResponse.data ?: emptyList()
                            val updatedProductList = productListDto.map {
                                ProductModel(
                                    productId = it.productId,
                                    listId = it.listId,
                                    name = it.name,
                                    quantity = it.quantity,
                                    originPrice = it.originPrice,
                                    createdAt = it.createdAt,
                                    deletedYn = it.deletedYn
                                )
                            }
                            Log.d("SlideCameraList", "최신 리스트 불러오기 성공, ${updatedProductList.size}개 아이템")
                            productAdapter.updateListCamera(updatedProductList.toMutableList())
                            onComplete?.invoke()
                            val result = Bundle().apply {
                                putBoolean("productAdded", true)
                            }
                            parentFragmentManager.setFragmentResult("requestKey", result)
                        } else {
                            Log.e("SlideCameraList", "상품 목록 불러오기 실패: ${apiResponse?.message}")
                        }
                    } else {
                        Log.e("SlideCameraList", "응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<ProductResponseDto>>>, t: Throwable) {
                    Log.e("SlideCameraList", "상품 목록 서버 요청 실패: ${t.message}")
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }

}