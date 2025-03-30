package com.example.moneychanger.etc

import android.util.Log
import com.example.moneychanger.network.currency.CurrencyManager

object ExchangeRateUtil {
    fun calculate(fromId: Long, toId: Long, amount: Double): Double {
        val fromCurrency = CurrencyManager.getById(fromId)
        val toCurrency = CurrencyManager.getById(toId)

        if (fromCurrency == null || toCurrency == null) {
            Log.e("ExchangeRate", "❗ 통화 정보 조회 실패: from=$fromId, to=$toId")
            return 0.0
        }

        val rateFrom = fromCurrency.dealBasR
        val rateTo = toCurrency.dealBasR

        if (rateFrom == 0.0 || rateTo == 0.0) {
            Log.e("ExchangeRate", "❗ 환율 값 오류: rateFrom=$rateFrom, rateTo=$rateTo")
            return 0.0
        }

        val fromUnitFactor = if (fromCurrency.curUnit.contains("(100)")) 100.0 else 1.0
        val toUnitFactor = if (toCurrency.curUnit.contains("(100)")) 100.0 else 1.0

        val adjustedRateFrom = rateFrom / fromUnitFactor
        val adjustedRateTo = rateTo / toUnitFactor

        val exchangedAmount = (amount * adjustedRateFrom) / adjustedRateTo

        Log.d(
            "ExchangeRate",
            "💱 환율 적용: ${fromCurrency.curUnit} ($rateFrom) → ${toCurrency.curUnit} ($rateTo), 금액 $amount → $exchangedAmount"
        )

        return exchangedAmount
    }
}
