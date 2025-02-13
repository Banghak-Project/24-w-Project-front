package com.example.moneychanger.network

import com.example.moneychanger.network.*

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/signin")
    suspend fun signIn(@Body signInRequest: SignInRequest): SignInResponse

    @POST("/api/auth/signup")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): SignUpResponse

    @POST("/api/auth/signup/otp")
    suspend fun sendOtp(@Body emailRequest: EmailRequest): Response<Void>

    @POST("/api/auth/signup/otp/check")
    suspend fun verifyOtp(@Body otpRequest: OtpRequest): String

    @POST("/api/auth/kakao/signin")
    suspend fun kakaoSignIn(@Body request: KakaoLoginRequest): KakaoLoginResponse
    @GET("/api/products")
    fun getAllProducts(): Call<List<ProductModel>>
}

// 데이터 클래스 정의
data class SignInRequest(val userEmail: String, val userPassword: String)
data class SignInResponse(val msg: String, val accessToken: String?, val refreshToken: String?)
data class SignUpRequest(val userEmail: String, val userPassword: String, val otp: String, val agreedTerms: List<Boolean>)
data class SignUpResponse(val msg: String)
data class EmailRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
data class KakaoLoginRequest(val accessToken: String)
data class KakaoLoginResponse(val msg: String, val accessToken: String?, val refreshToken: String?)
