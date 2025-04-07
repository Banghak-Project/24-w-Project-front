package com.example.moneychanger.etc

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moneychanger.databinding.SlideProductEditBinding
import com.example.moneychanger.network.product.ProductModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SlideProductEdit : BottomSheetDialogFragment() {

    private lateinit var binding: SlideProductEditBinding
    private var product: ProductModel? = null

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
        }

        // 확인 버튼 클릭 시
        binding.buttonAdd.setOnClickListener {
            val newName = binding.inputName.text.toString()
            val newPriceStr = binding.inputPrice.text.toString()

            if (newName.isBlank() || newPriceStr.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPrice = newPriceStr.toDoubleOrNull()
            if (newPrice == null) {
                Toast.makeText(requireContext(), "가격을 숫자로 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: 이곳에서 서버로 수정 요청하거나 콜백을 통해 상위에서 처리할 수 있도록 구현
            Toast.makeText(requireContext(), "수정 완료 (임시 메시지)", Toast.LENGTH_SHORT).show()
            dismiss()
        }

    }

    companion object {
        const val TAG = "SlideProductEdit"
    }
}

fun dpToPx(dp: Float): Float {
    return dp * Resources.getSystem().displayMetrics.density
}