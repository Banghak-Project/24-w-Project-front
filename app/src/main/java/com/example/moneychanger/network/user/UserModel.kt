package com.example.moneychanger.network.user

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("userName") val userName: String,
    @SerializedName("userDateOfBirth") val userDateOfBirth: Long, // "yyyy-MM-dd" 포맷 유지
    @SerializedName("userGender") val userGender: Boolean, // true: 남성, false: 여성
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userPassword") val userPassword: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("agreedTerms") val agreedTerms: List<Boolean>,
    @SerializedName("defaultCurrencyId") val defaultCurrencyId: Long
)

data class SignUpResponse(
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userGender") val userGender: Boolean? = null,
    @SerializedName("userDateOfBirth") val userDateOfBirth: String? = null,
    @SerializedName("defaultCurrencyId") val defaultCurrencyId: Long? = null

)

data class SignInRequest(@SerializedName("userEmail") val userEmail: String? = "",
                         @SerializedName("userPassword") val userPassword: String)
data class SignInResponse(  @SerializedName("userId") val userId: Long,
                            @SerializedName("userName") val userName: String? = null,
                            @SerializedName("userEmail") val userEmail: String? = null,
                            @SerializedName("msg") val msg: String? = null,
                            @SerializedName("accessToken") val accessToken: String? = null,
                            @SerializedName("refreshToken") val refreshToken: String? = null,
                            @SerializedName("kakaoAccessToken") val kakaoAccessToken: String? = null,
                            @SerializedName("firstSocialLogin") val firstSocialLogin: Boolean? = null,
                            @SerializedName("socialProvider") val socialProvider: String? = null
    )

data class UserInfoResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("userName") val userName: String,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userDateOfBirth") val userDateOfBirth: String?,
    @SerializedName("kakaoUser") val isKakaoUser: Boolean,
    @SerializedName("googleUser") val isGoogleUser: Boolean,
    @SerializedName("defaultCurrencyId") val defaultCurrencyId: Long
)
data class UpdateUserInfoRequest(
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userDateOfBirth") val userDateOfBirth: String?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("userPassword") val userPassword: String?,
    @SerializedName("defaultCurrencyId") val defaultCurrencyId: Long
)

data class EmailRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
data class KakaoLoginRequest(
    @SerializedName("access_token") val accessToken: String
)

data class FindPasswordRequest(
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("inputOtp") val inputOtp: String? = null
)

data class ResetPasswordRequest(
    val userEmail: String,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)





