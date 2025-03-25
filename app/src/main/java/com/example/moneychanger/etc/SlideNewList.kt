package com.example.moneychanger.etc

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moneychanger.databinding.SlideNewListBinding
import com.example.moneychanger.network.CurrencyStoreManager
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.network.list.CreateListRequestDto
import com.example.moneychanger.network.list.CreateListResponseDto
import com.example.moneychanger.network.list.CreateListWithNameRequestDto
import com.example.moneychanger.network.user.ApiResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SlideNewList : BottomSheetDialogFragment() {
    private var _binding: SlideNewListBinding? = null
    private val binding get() = _binding!!

    private var listener: OnStoreNameUpdatedListener? = null

    private var currencyIdFrom = -1L
    private var currencyIdTo = -1L
    private val userId = TokenManager.getUserId() ?: -1L
    private val location = "Seoul"

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

        // 통화 정보 가져오기
        val currencyList = CurrencyStoreManager.getCurrencyList()

        if (currencyList.isNullOrEmpty()) {
            context?.let {
                Toast.makeText(it, "로그인 후 이용해주세요.", Toast.LENGTH_LONG).show()
            }
            return
        }

        // 통화 Spinner 데이터 설정
        val currencyUnits: List<String> = currencyList?.mapNotNull { it.curUnit } ?: emptyList()
        val customSpinner1 = CustomSpinner(requireContext(), currencyUnits)
        val customSpinner2 = CustomSpinner(requireContext(), currencyUnits)

        binding.currencyContainer1.setOnClickListener {
            customSpinner1.show(binding.currencyContainer1) { selected ->
                binding.currencyName1.text = selected
                val selectedCurrency = CurrencyStoreManager.findCurrencyByUnit(selected)
                if (selectedCurrency != null) {
                    currencyIdFrom = selectedCurrency.currentId
                }
            }
        }

        binding.currencyContainer2.setOnClickListener {
            customSpinner2.show(binding.currencyContainer2) { selected ->
                binding.currencyName2.text = selected
                val selectedCurrency = CurrencyStoreManager.findCurrencyByUnit(selected)
                if (selectedCurrency != null) {
                    currencyIdTo = selectedCurrency.currentId
                }
            }
        }

        binding.buttonAdd.setOnClickListener {
            if (currencyIdFrom == -1L || currencyIdTo == -1L) {
                context?.let {
                    Toast.makeText(it, "두 통화를 모두 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val storeName = binding.textStoreName.text.toString()

            addNewList(userId, storeName, currencyIdFrom, currencyIdTo, location)

            // ListActivity로 데이터 전달
            listener?.onStoreNameUpdated(storeName)

            dismiss() // Bottom Sheet 닫기
        }
    }

    private fun addNewList(userId: Long, storeName: String,currencyIdFrom: Long, currencyIdTo: Long, location: String) {
        val createRequest = CreateListWithNameRequestDto(userId, storeName, currencyIdFrom, currencyIdTo, location)
        Log.d("CameraActivity", "🚀 리스트 생성 요청 데이터: userId=$userId, currencyIdFrom=$currencyIdFrom, currencyIdTo=$currencyIdTo, location=$location")

        // 리스트 추가 API 호출 (비동기 방식)
        RetrofitClient.apiService.createListWithName(createRequest)
            .enqueue(object : Callback<ApiResponse<CreateListResponseDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<CreateListResponseDto>>,
                    response: Response<ApiResponse<CreateListResponseDto>>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == "success") {
                            val jsonData = Gson().toJson(apiResponse.data)
                            val createListResponse: CreateListResponseDto? = try {
                                Gson().fromJson(jsonData, CreateListResponseDto::class.java)
                            } catch (e: JsonSyntaxException) {
                                Log.e("CameraActivity", "🚨 JSON 변환 오류: ${e.message}")
                                null
                            }

                            if (createListResponse != null) {
                                val listId = createListResponse.listId ?: -1L
                                if (listId != -1L) {
                                    context?.let {
                                        Toast.makeText(it, "리스트 추가 완료!", Toast.LENGTH_SHORT).show()
                                    }
                                    Log.d("CameraActivity", "✅ 리스트 생성 성공: ID=$listId")
                                } else {
                                    Log.e("CameraActivity", "🚨 리스트 ID 오류 발생")
                                }
                            } else {
                                Log.e("CameraActivity", "🚨 리스트 응답 데이터 변환 실패")
                            }
                        } else {
                            Log.e("CameraActivity", "🚨 리스트 추가 실패: ${apiResponse?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        Log.e("CameraActivity", "🚨 응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CreateListResponseDto>>, t: Throwable) {
                    Log.e("CameraActivity", "🚨 서버 요청 실패: ${t.message}")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
