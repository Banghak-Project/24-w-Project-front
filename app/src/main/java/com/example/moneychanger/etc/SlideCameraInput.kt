package com.example.moneychanger.etc

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneychanger.databinding.SlideCameraInputBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SlideCameraInput : BottomSheetDialogFragment() {
    private var _binding: SlideCameraInputBinding? = null
    private val binding get() = _binding!!

    private var listener: OnProductAddedListener? = null

    companion object {
        const val TAG = "SlideCameraInput"
    }

    override fun onStart() {
        super.onStart()

        // BottomSheetDialog의 높이를 최대 크기로 설정
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = resources.displayMetrics.heightPixels * 1 / 2
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
        Log.d("SlideCameraInput", "onCreateView 실행됨")
        _binding = SlideCameraInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.setOnClickListener {
            val productName = binding.inputName.text.toString()
            val priceText = binding.inputPrice.text.toString()

            // ✅ Double 변환 (예외 발생 방지)
            val price: Double = try {
                priceText.replace(",", "").toDouble()
            } catch (e: NumberFormatException) {
                0.0 // 변환 실패 시 기본값 설정
            }

            listener?.onProductAdded(productName, price)

            dismiss() // ✅ Bottom Sheet 닫기
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}

interface OnProductAddedListener {
    fun onProductAdded(productName: String, price: Double)
}

