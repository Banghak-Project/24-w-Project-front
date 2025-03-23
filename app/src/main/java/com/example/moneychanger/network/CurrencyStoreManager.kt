package com.example.moneychanger.network

import com.example.moneychanger.network.currency.CurrencyResponseDto


object CurrencyStoreManager {
    private var currencyList: List<CurrencyResponseDto>? = null

    // 통화 정보 저장
    fun saveCurrencyList(currencies: List<CurrencyResponseDto>) {
        currencyList = currencies
    }

    // 저장된 통화 정보 가져오기
    fun getCurrencyList(): List<CurrencyResponseDto>? {
        return currencyList
    }

    fun findCurrencyByUnit(unit: String): CurrencyResponseDto? {
        return currencyList?.find { it.curUnit == unit }
    }

    // 통화 ID로 찾기
    fun findCurrencyById(id: Long): CurrencyResponseDto? {
        return currencyList?.find { it.currentId == id }
    }

    // 통화 정보 초기화 (로그아웃 시)
    fun clearCurrencyList() {
        currencyList = null
    }
}
