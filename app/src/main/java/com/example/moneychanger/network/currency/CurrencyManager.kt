package com.example.moneychanger.network.currency

import com.example.moneychanger.network.currency.CurrencyModel

object CurrencyManager {
    private val currencyList = mutableListOf<CurrencyModel>()

    fun setCurrencies(currencies: List<CurrencyModel>) {
        currencyList.clear()
        currencyList.addAll(currencies)
    }

    fun getAll(): List<CurrencyModel> {
        return currencyList
    }

    fun getById(id: Long): CurrencyModel? {
        return currencyList.find { it.currencyId == id }
    }

    fun getByCode(code: String): CurrencyModel? {
        return currencyList.find { it.curUnit == code }
    }
}
