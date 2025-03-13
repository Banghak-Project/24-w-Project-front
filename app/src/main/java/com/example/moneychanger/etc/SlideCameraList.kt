package com.example.moneychanger.etc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.databinding.SlideCameraListBinding
import com.example.moneychanger.network.product.ProductModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SlideCameraList : BottomSheetDialogFragment() {
    private var _binding: SlideCameraListBinding? = null
    private val binding get() = _binding!!

    private var listener: OnProductAddedListener? = null
    private lateinit var productAdapter: ProductAdapter  // ✅ 기존 ProductAdapter 사용

    companion object {
        const val TAG = "SlideCameraList"

        fun newInstance(productList: MutableList<ProductModel>): SlideCameraList {
            val fragment = SlideCameraList()
            val args = Bundle()
            args.putParcelableArrayList("product_list", ArrayList(productList))
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SlideCameraListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 전달받은 데이터 가져오기
        val productList = arguments?.getParcelableArrayList<ProductModel>("product_list")?.toMutableList() ?: mutableListOf()

        // ✅ RecyclerView 설정
        productAdapter = ProductAdapter(productList)
        binding.productContainer.adapter = productAdapter

        // ✅ 추가하기 버튼 클릭 시
        binding.buttonAdd.setOnClickListener {
            val slideCameraInput = SlideCameraInput()  // ✅ 입력 창으로 이동
            slideCameraInput.show(parentFragmentManager, SlideCameraInput.TAG)  // ✅ parentFragmentManager 사용
            dismiss() // ✅ 기존 리스트 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}