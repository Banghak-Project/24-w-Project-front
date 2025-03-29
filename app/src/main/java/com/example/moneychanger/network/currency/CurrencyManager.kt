package com.example.moneychanger.network.currency

import android.util.Log

object CurrencyManager {
    private val currencyList = mutableListOf<CurrencyModel>()

    fun setCurrencies(currencies: Collection<CurrencyModel>) {
        currencyList.clear()
        currencyList.addAll(currencies)
    }
    fun getCurrencies(): List<CurrencyModel>{
        return currencyList
    }

    fun getById(id: Long): CurrencyModel? {
        val found = currencyList.find { it.currencyId == id }
        if (found == null) {
            Log.e("CurrencyManager", "⚠️ 통화 정보 매핑 실패: id=$id")
        }
        return found
    }

    fun getByUnit(code: String): CurrencyModel {
        return currencyList.find { it.curUnit == code }!!
    }
}
