package com.example.moneychanger.network.user

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("userName") val userName: String,
    @SerializedName("userDateOfBirth") val userDateOfBirth: Long, // "yyyy-MM-dd" 포맷 유지
    @SerializedName("userGender") val userGender: Boolean, // true: 남성, false: 여성
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userPassword") val userPassword: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("agreedTerms") val agreedTerms: List<Boolean>
)

data class SignUpResponse(
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userGender") val userGender: Boolean? = null,
    @SerializedName("userDateOfBirth") val userDateOfBirth: String? = null
)

data class SignInRequest(@SerializedName("userEmail") val userEmail: String? = "",
                         @SerializedName("userPassword") val userPassword: String)
data class SignInResponse(  @SerializedName("userId") val userId: Long,
                            @SerializedName("userName") val userName: String? = null,
                            @SerializedName("msg") val msg: String? = null,
                            @SerializedName("accessToken") val accessToken: String? = null,
                            @SerializedName("refreshToken") val refreshToken: String? = null,
                            @SerializedName("kakaoAccessToken") val kakaoAccessToken: String? = null)



data class EmailRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
data class KakaoLoginRequest(
    @SerializedName("access_token") val accessToken: String
)

data class KakaoLoginResponse(
    val userId: Long?,
    val userName: String?,
    val msg: String?,
    val accessToken: String?,
    val refreshToken: String?
)
