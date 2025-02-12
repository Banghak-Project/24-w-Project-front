package com.example.moneychanger.etc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneychanger.databinding.SlideEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SlideEdit : BottomSheetDialogFragment() {
    private var _binding: SlideEditBinding? = null
    private val binding get() = _binding!!

    private var listener: OnStoreNameUpdatedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStoreNameUpdatedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnStoreNameUpdatedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SlideEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonUpdate.setOnClickListener {
            val storeName = binding.textStoreName.text.toString()

            // ListActivity로 데이터 전달
            listener?.onStoreNameUpdated(storeName)

            dismiss() // Bottom Sheet 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}

interface OnStoreNameUpdatedListener {
    fun onStoreNameUpdated(storeName: String)
}

