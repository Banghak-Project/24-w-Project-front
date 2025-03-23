package com.example.moneychanger.etc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneychanger.databinding.SlideNewListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SlideNewList : BottomSheetDialogFragment() {
    private var _binding: SlideNewListBinding? = null
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
        _binding = SlideNewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 통화 Spinner 데이터 설정
        val currencyItems = listOf("KRW", "JPY", "USD", "THB", "ITL", "UTC", "FRF", "GBP", "CHF", "VND", "AUD")
        val customSpinner1 = CustomSpinner(requireContext(), currencyItems)
        val customSpinner2 = CustomSpinner(requireContext(), currencyItems)

        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                // 필요 시 ViewModel 처리 또는 callback 사용
            }
        }

        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                // 필요 시 ViewModel 처리 또는 callback 사용
            }
        }

        binding.buttonAdd.setOnClickListener {
            val storeName = binding.textStoreName.text.toString()

            // 이 부분에 db 업데이트 코드 추가
            // ex) updateStoreNameInDB(storeName)

            // ListActivity로 데이터 전달
            listener?.onStoreNameUpdated(storeName)

            dismiss() // Bottom Sheet 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
