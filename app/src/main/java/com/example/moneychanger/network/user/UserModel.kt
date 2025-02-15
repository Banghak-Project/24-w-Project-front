package com.example.moneychanger.network.user

data class SignInRequest(val userEmail: String, val userPassword: String)
data class SignInResponse(val msg: String, val accessToken: String?, val refreshToken: String?)
data class SignUpRequest(val userEmail: String, val userPassword: String, val otp: String, val agreedTerms: List<Boolean>)
data class SignUpResponse(val msg: String)
data class EmailRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
data class KakaoLoginRequest(val accessToken: String)
data class KakaoLoginResponse(val msg: String, val accessToken: String?, val refreshToken: String?)
