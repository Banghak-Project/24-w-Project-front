package com.example.moneychanger.network.list

import com.google.gson.annotations.SerializedName

import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.currency.CurrencyResponseDto

data class ListModel(
    val listId: Long,
    val name: String,
    val userId: Long,
    val createdAt: String?,
    val location: String,
    val currencyFrom: CurrencyModel,
    val currencyTo: CurrencyModel,
    val deletedYn: Boolean
)

data class ListsRequestDto(
    val userId : Long,
    val currencyIdFrom : Long,
    val currencyIdTo: Long,
    val location : String //location은 추후 api 개발되면 requestdto에서 뺌
)

data class ListsResponseDto(
    @SerializedName("listId") val listId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("location") val location: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("currencyFromId") val currencyFromId: Long,
    @SerializedName("currencyToId") val currencyToId: Long,
    @SerializedName("deletedYn") val deletedYn: Boolean
)


data class UpdateRequestDto(
    val currencyIdFrom: Long?,
    val currencyIdTo: Long?,
    val location: String?,
    val name:String?
)

data class CreateListRequestDto(
    @SerializedName("userId") val userId: Long,
    @SerializedName("currencyIdFrom") val currencyIdFrom: Long,
    @SerializedName("currencyIdTo") val currencyIdTo: Long,
    @SerializedName("location") val location: String
)

data class CreateListWithNameRequestDto(
    @SerializedName("userId") val userId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("currencyIdFrom") val currencyIdFrom: Long,
    @SerializedName("currencyIdTo") val currencyIdTo: Long,
    @SerializedName("location") val location: String
)

data class CreateListResponseDto(
    @SerializedName("listId") val listId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String,  // 서버에서 "2025-03-18T17:35:04.933048" 형식으로 전달되므로 String 사용
    @SerializedName("location") val location: String,
    @SerializedName("currencyFrom") val currencyFrom: Long,
    @SerializedName("currencyTo") val currencyTo: Long,
    @SerializedName("deletedYn") val deletedYn: Boolean
)
