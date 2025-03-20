package com.example.moneychanger.network.product;

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ProductModel(
    val productId: Long,
    val listId: Long, // 외래 키 추가 - 유빈
    val name: String,
    val originPrice: Double,
    val deletedYn : Boolean
): Parcelable
data class ProductRequestDto(
    val listId: Long,
    val originPrice: Double,
    val deletedYn : Boolean
)
data class ProductResponseDto(
    val productId: Long,
    val listId: Long,
    val name: String,
    val originPrice: Double,
    val deletedYn : Boolean
)
data class ImageProductResponseDto(
    val name: String,
    val price: Double,
)