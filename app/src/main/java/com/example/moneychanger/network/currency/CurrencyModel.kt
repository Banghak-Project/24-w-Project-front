package com.example.moneychanger.network.currency

import com.google.gson.annotations.SerializedName

data class CurrencyModel(
    val currencyId: Long,
    val curUnit: String,
    val dealBasR: Double,
    val curNm: String
) {
    override fun toString(): String {
        return "$curUnit ($curNm)"
    }
}
)

data class CurrencyResponseDto(
    @SerializedName("currentId") val currentId: Long,
    @SerializedName("curUnit") val curUnit: String,
    @SerializedName("dealBasR") val dealBasR: String,
    @SerializedName("curNm") val curNm: String
)
