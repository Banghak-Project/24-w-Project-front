package com.example.moneychanger.network.currency

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CurrencyModel(
    val currencyId: Long,
    val curUnit: String,
    val dealBasR: Double,
    val curNm: String
) : Serializable {
    override fun toString(): String {
        return "$curUnit ($curNm)"
    }
}

data class CurrencyResponseDto(
    @SerializedName("currencyId") val currencyId: Long,
    @SerializedName("cur_unit") val curUnit: String,
    @SerializedName("deal_bas_r") val dealBasR: String,
    @SerializedName("cur_nm") val curNm: String
)
