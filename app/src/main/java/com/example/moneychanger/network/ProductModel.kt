package com.example.moneychanger.network;

data class ProductModel(
    val productId: Int,       // JSON의 productId와 일치
    val name: String,         // JSON의 name과 일치
    val originPrice: Double,  // JSON의 originPrice와 일치
    val convertedPrice: Double,  // JSON의 convertedPrice와 일치
    val currencyRate: Double  // JSON의 currencyRate와 일치
)
