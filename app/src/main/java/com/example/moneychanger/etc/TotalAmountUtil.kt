package com.example.moneychanger.etc

import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.user.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TotalAmountUtil {
    fun fetchTotalAmount(listId: Long, onResult: (Double) -> Unit) {
        RetrofitClient.apiService.getTotal(listId).enqueue(object : Callback<ApiResponse<Double>> {
            override fun onResponse(
                call: Call<ApiResponse<Double>>,
                response: Response<ApiResponse<Double>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val totalAmount = response.body()?.data ?: 0.0
                    onResult(totalAmount)
                } else {
                    onResult(0.0)
                }
            }
            override fun onFailure(call: Call<ApiResponse<Double>>, t: Throwable) {
                onResult(0.0)
            }
        })
    }
}
