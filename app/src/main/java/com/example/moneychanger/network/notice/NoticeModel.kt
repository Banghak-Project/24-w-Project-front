package com.example.moneychanger.network.notice

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NoticeModel (
    val noticeId: Long,
    val title: String,
    val content: String
):Serializable

data class NoticeResponseDto(
    @SerializedName("notice_id") val noticeId:Long,
    @SerializedName("title") val title:String,
    @SerializedName("content") val content:String,
    @SerializedName("date") val date:String
):Serializable {
    var isExpanded: Boolean = false
}
