package com.example.moneychanger.network.list

import com.example.moneychanger.network.currency.CurrencyModel

data class ListModel(
    val listId: Long,
    val name: String,
    val userId: Long,
    val createdAt: String,
    val location: String,
    val currencyFrom: CurrencyModel?,
    val currencyTo: CurrencyModel?,
    val deletedYn: Boolean
)

data class ListsRequestDto(
    val userId : Long,
    val currencyIdFrom : Long,
    val currencyIdTo: Long,
    val location : String //location은 추후 api 개발되면 requestdto에서 뺌
)

data class ListsResponseDto(
    val listId: Long,
    val name: String,
    val userId : Long,
    val location: String,
    val createdAt: String,
    val currencyFrom : CurrencyModel,
    val currencyTo: CurrencyModel,
    val deletedYn: Boolean
)
