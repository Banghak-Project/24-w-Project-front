package com.example.moneychanger

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductViewModel : ViewModel() {

    private val _products = MutableLiveData<List<ProductModel>>()
    val products: LiveData<List<ProductModel>> get() = _products

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage:LiveData<String> get() = _errorMessage

    fun fetchProducts(){
        RetrofitClient.apiService.getAllProducts().enqueue(object :Callback<List<ProductModel>>{
            override fun onResponse(
                call: Call<List<ProductModel>>,
                response: Response<List<ProductModel>>
            ) {
                if(response.isSuccessful){
                    Log.v("info", "서버 응답 성공: ${response.body()}")
                    _products.value = response.body()
                }else{
                    Log.e("info", "서버 응답 오류: ${response.code()}, ${response.message()}")
                    _errorMessage.value = "서버 응답 오류: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<ProductModel>>, t: Throwable) {
                Log.e("info", "네트워크 오류 발생: ${t.message}")
                _errorMessage.value = "네트워크 오류: ${t.message}"
            }
        })

    }
}