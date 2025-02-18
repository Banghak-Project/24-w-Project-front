package com.example.moneychanger.network.list

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
