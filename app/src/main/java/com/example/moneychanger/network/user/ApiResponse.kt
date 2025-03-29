package com.example.moneychanger.network.user

import com.google.gson.annotations.SerializedName

/**
 * ✅ 모든 API 응답을 감싸는 공통 Response 클래스
 * 백엔드의 응답 형식과 동일하게 맞춰줌
 */
data class ApiResponse<T>(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: T?,
    @SerializedName("message") val message: String? = null
)
