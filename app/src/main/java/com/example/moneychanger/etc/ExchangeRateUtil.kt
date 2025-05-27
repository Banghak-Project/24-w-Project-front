// ExchangeRateUtil.kt
package com.example.moneychanger.etc

import android.util.Log
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.user.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ExchangeRateUtil {
    fun calculateExchangeRate(fromId: Long, toId: Long, amount: Double): Double {
        val fromCurrency = CurrencyManager.getById(fromId)
        val toCurrency = CurrencyManager.getById(toId)

        if (fromCurrency == null || toCurrency == null) {
            Log.e("ExchangeRate", "통화 정보 매핑 실패")
            return 0.0
        }

        val rateFrom = fromCurrency.dealBasR
        val rateTo = toCurrency.dealBasR

        if (rateFrom == 0.0 || rateTo == 0.0) {
            Log.e("ExchangeRate", "환율 값이 유효하지 않음: rateFrom=$rateFrom, rateTo=$rateTo")
            return 0.0
        }

        val fromDivisor = if (fromCurrency.curUnit.contains("(100)")) 100.0 else 1.0
        val toDivisor = if (toCurrency.curUnit.contains("(100)")) 100.0 else 1.0

        val adjustedRateFrom = rateFrom / fromDivisor
        val adjustedRateTo = rateTo / toDivisor

        val exchangedAmount = (amount * adjustedRateFrom) / adjustedRateTo
        Log.d("ExchangeRate", "계산됨: $amount → $exchangedAmount")
        return exchangedAmount
    }


    suspend fun getExchangeRate(fromId: Long, toId: Long):Double{
        return withContext(Dispatchers.Default) {
            val fromCurrency = CurrencyManager.getById(fromId)
            val toCurrency = CurrencyManager.getById(toId)

            if (fromCurrency == null || toCurrency == null) {
                Log.e("ExchangeRate", "통화 정보 매핑 실패")
                return@withContext 0.0
            }

            val rateFrom = fromCurrency.dealBasR
            val rateTo = toCurrency.dealBasR

            if (rateFrom == 0.0 || rateTo == 0.0) {
                Log.e("ExchangeRate", "환율 값이 유효하지 않음: rateFrom=$rateFrom, rateTo=$rateTo")
                return@withContext 0.0
            }

            val fromDivisor = if (fromCurrency.curUnit.contains("(100)")) 100.0 else 1.0
            val toDivisor = if (toCurrency.curUnit.contains("(100)")) 100.0 else 1.0

            val adjustedRateFrom = rateFrom / fromDivisor
            val adjustedRateTo = rateTo / toDivisor

            val exchangedAmount = (1 * adjustedRateFrom) / adjustedRateTo
            exchangedAmount
        }
    }

    fun getUserDefaultCurrency(onFinished: (Long?) -> Unit){
        RetrofitClient.apiService.getUserCurrency().enqueue(object : Callback<ApiResponse<Long>> {
            override fun onResponse(
                call: Call<ApiResponse<Long>>,
                response: Response<ApiResponse<Long>>
            ){
                if (response.isSuccessful){
                    val currencyId = response.body()?.data
                    onFinished(currencyId)
                }
                else{
                    Log.e("DashboardActivity", "통화 ID 조회 실패: ${response.code()}")
                    onFinished(null)
                }
            }

            override fun onFailure(p0: Call<ApiResponse<Long>>, t: Throwable) {
                Log.e("DashboardActivity", "API 호출 실패: ${t.message}")
                onFinished(null)
            }
        })
    }

}
