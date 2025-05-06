// ExchangeRateUtil.kt
package com.example.moneychanger.etc

import android.util.Log
import com.example.moneychanger.network.currency.CurrencyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExchangeRateUtil {
    suspend fun calculateExchangeRate(fromId: Long, toId: Long, amount: Double): Double {
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

            val exchangedAmount = (amount * adjustedRateFrom) / adjustedRateTo
            Log.d("ExchangeRate", "계산됨: $amount → $exchangedAmount")
            exchangedAmount
        }
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

}
