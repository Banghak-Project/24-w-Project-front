package com.example.moneychanger.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Retrofit 인터페이스 정의
interface ApiService {
    @POST("/api/auth/signin")
    suspend fun signIn(@Body signInRequest: SignInRequest): SignInResponse

    @POST("/api/auth/signup")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): SignUpResponse

    @POST("/api/auth/signup/otp")
    suspend fun sendOtp(@Body emailRequest: EmailRequest): String

    @POST("/api/auth/signup/otp/check")
    suspend fun verifyOtp(@Body otpRequest: OtpRequest): String

    @POST("/api/auth/kakao/signin")
    suspend fun kakaoSignIn(@Body request: RetrofitClient.KakaoLoginRequest): RetrofitClient.KakaoLoginResponse
}

// Retrofit 클라이언트 객체 생성
object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/" // 백엔드 URL

    val apiService: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    data class KakaoLoginRequest(
        val accessToken: String
    )

    data class KakaoLoginResponse(
        val msg: String,
        val accessToken: String?,
        val refreshToken: String?
    )
}

// 데이터 클래스 정의
data class SignInRequest(val email: String, val password: String)
data class SignInResponse(val msg: String, val token: String)
data class SignUpRequest(val email: String, val password: String, val otp: String, val agreedTerms: List<Boolean>)
data class SignUpResponse(val msg: String)
data class EmailRequest(val email: String)
data class OtpRequest(val email: String, val otp: String)
