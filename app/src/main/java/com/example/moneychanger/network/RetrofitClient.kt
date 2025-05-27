package com.example.moneychanger.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
//    private const val BASE_URL = "http://172.16.214.142"
    private const val BASE_URL = "http://10.0.2.2:8080/"
    // 애뮬레이터에서 실행하는 거면 이거 사용
    //실제 기기에서 돌릴때는 PC의 로컬 IP 주소 사용해야한다고 함.
    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }) // Retrofit API 요청/응답 로그 확인 가능
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .serializeNulls() // null 값도 JSON에 포함
        .setLenient()  // JSON 파싱 오류 방지
        .setDateFormat("yyyy-MM-dd") // 날짜 포맷 설정
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) // 스네이크 케이스로,, 모두 바꿔야함
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON 응답 지원
            .build()
            .create(ApiService::class.java)
    }

    val imageApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // JSON 변환
            .build()
            .create(ApiService::class.java)
    }
}

// 토큰 자동 추가 Interceptor
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getAccessToken()
        val request = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}
