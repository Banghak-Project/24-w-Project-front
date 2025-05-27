package com.example.moneychanger.etc

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.databinding.SlideCameraCountBinding
import com.example.moneychanger.databinding.SlideCameraListBinding
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class SlideCameraCount : BottomSheetDialogFragment() {
    private var _binding: SlideCameraCountBinding? = null
    private val binding get() = _binding!!

    var productAddListener: OnProductAddListener? = null


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
        _binding = SlideCameraCountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.buttonAdd.setOnClickListener {
            productAddListener?.onProductAdd()
            dismiss()
        }

        var pieces = 1
        binding.countText.text = pieces.toString()
        binding.buttonMinus.setOnClickListener {
            if (pieces > 1) {
                pieces -= 1
                binding.countText.text = pieces.toString()
            }

            if (pieces > 1) {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
            } else {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_02)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_02)
                )
            }
        }

        binding.buttonPlus.setOnClickListener {
            pieces += 1
            binding.countText.text = pieces.toString()

            if (pieces > 1) {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
            }else {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_03)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_03)
                )
            }
        }
    }

    interface OnProductAddListener {
        fun onProductAdd()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }

}