package com.example.moneychanger.network.currency

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData;

class CurrencyViewModel : ViewModel() {
    private val _selectedCurrency = MutableLiveData<String>()
    val selectedCurrency: LiveData<String> get() = _selectedCurrency

    private val _currencyList = MutableLiveData<List<CurrencyModel>>()
    val currencyList: LiveData<List<CurrencyModel>> get() = _currencyList

    fun updateCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun getCurrencyIdByUnit(unit: String): Long? {
        return _currencyList.value?.firstOrNull { it.curUnit == unit }?.currencyId

    }


}
