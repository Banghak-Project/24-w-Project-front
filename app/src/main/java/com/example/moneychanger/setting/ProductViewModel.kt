package com.example.moneychanger

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.user.ApiResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductViewModel : ViewModel() {

    private val _products = MutableLiveData<List<ProductModel>>()
    val products: LiveData<List<ProductModel>> get() = _products

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage:LiveData<String> get() = _errorMessage

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val response: Response<ApiResponse<Call<List<ProductModel>>>> = RetrofitClient.apiService.getAllProducts()

                if (response.isSuccessful) {
                    val apiResponse: ApiResponse<Call<List<ProductModel>>>? = response.body()
                    if (apiResponse?.status == "success") {
                        _products.value = (apiResponse.data as? List<ProductModel>) ?: emptyList()
                        Log.v("info", "서버 응답 성공: ${apiResponse.data}")
                    } else {
                        Log.e("info", "서버 응답 실패: ${apiResponse?.message}")
                        _errorMessage.value = apiResponse?.message ?: "알 수 없는 오류"
                    }
                } else {
                    Log.e("info", "서버 응답 오류: ${response.code()}, ${response.message()}")
                    _errorMessage.value = "서버 응답 오류: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("info", "네트워크 오류 발생: ${e.message}")
                _errorMessage.value = "네트워크 오류: ${e.message}"
            }
        }
    }
}