package com.example.moneychanger.etc

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moneychanger.databinding.SlideEditBinding
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.UpdateRequestDto
import com.example.moneychanger.network.list.UpdateResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SlideEdit : BottomSheetDialogFragment() {
    private var _binding: SlideEditBinding? = null
    private val binding get() = _binding!!

    private var listener: OnStoreNameUpdatedListener? = null

    private lateinit var selectedList: ListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selectedList = it.getSerializable("selectedList") as ListModel
        }
    }

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

            updateListName(storeName)

            // ListActivity로 데이터 전달
            listener?.onStoreNameUpdated(storeName)
        }
    }

    private fun updateListName(storeName: String) {
        val updateRequest = UpdateRequestDto(
            listId = selectedList!!.listId,
            currencyIdFrom = selectedList!!.currencyFrom.currencyId,
            currencyIdTo = selectedList!!.currencyTo.currencyId,
            location = selectedList!!.location,
            name = storeName
        )

        RetrofitClient.apiService.updateList(updateRequest)
            .enqueue(object : Callback<ApiResponse<UpdateResponseDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<UpdateResponseDto>>,
                    response: Response<ApiResponse<UpdateResponseDto>>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Log.i("ListActivity", "✅ 서버에 리스트 업데이트 완료")

                        (activity as? ListActivity)?.setResult(RESULT_OK)
                        val result = Bundle().apply {
                            putBoolean("listNameUpdated", true)
                        }
                        parentFragmentManager.setFragmentResult("requestKey", result)
                        dismiss() // Bottom Sheet 닫기
                    } else {
                        Log.e("ListActivity", "❌ 서버 응답 실패: ${response.errorBody()?.string()}")
                        context?.let {
                            Toast.makeText(it, "리스트 업데이트 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UpdateResponseDto>>, t: Throwable) {
                    Log.e("ListActivity", "❌ 서버 업데이트 실패", t)
                    context?.let {
                        Toast.makeText(it, "서버 통신 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}

interface OnStoreNameUpdatedListener {
    fun onStoreNameUpdated(storeName: String)
}

