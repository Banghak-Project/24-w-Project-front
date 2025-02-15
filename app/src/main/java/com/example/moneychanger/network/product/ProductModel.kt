package com.example.moneychanger.network.product;

data class ProductModel(
    val productId: Int,
    val name: String,
    val originPrice: Double
)
data class ProductRequestDto(
    val name: String,
    val originPrice: Double
)
data class ProductResponseDto(
    val productId: Long,
    val name: String,
    val originPrice: Double
)