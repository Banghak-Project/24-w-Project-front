package com.example.moneychanger.network.list

import com.google.gson.annotations.SerializedName

data class ListModel(
    val listId: Long,
    val name: String,
    val createdAt: String,
    val location: String,
    val deletedYn: Boolean
    // created_at이 없음 임시로 만들어둠 -유빈
)

data class ListsRequestDto(
    val name: String,
    val userId : Long,
    val currencyId : Long,
    val location : String // location은 추후 api 개발되면 requestdto에서 뺌
)

data class ListsResponseDto(
    val listId: Long,
    val name: String,
    val location: String
)

data class CreateListRequestDto(
    @SerializedName("userId") val userId: Long,
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
