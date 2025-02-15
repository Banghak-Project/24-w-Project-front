package com.example.moneychanger.network.currency

data class CurrencyModel(
    val currencyId: Long,
    val curUnit: String,
    val dealBasR: Double,
    val curNm: String
)