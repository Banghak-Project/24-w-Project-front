package com.example.moneychanger.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
//    const val BASE_URL = "http://3.34.157.74:8080/"
    const val BASE_URL = "http://10.0.2.2:8080/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor{
            chain ->
            val response = chain.proceed(chain.request())
            response.header("New-Access-Token")?.let { newToken ->
                TokenManager.saveAccessToken(newToken)
            }
            response
        }
        .authenticator(TokenAuthenticator())
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

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access = TokenManager.getAccessToken()
        val refresh = TokenManager.getRefreshToken()
        val req = chain.request().newBuilder().also { b ->
            if (!access.isNullOrEmpty())   b.addHeader("Authorization", "Bearer $access")
            if (!refresh.isNullOrEmpty())  b.addHeader("Refresh-Token",  "Bearer $refresh")
        }.build()
        return chain.proceed(req)
    }
}



class TokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) > 1) return null

        val refreshToken = TokenManager.getRefreshToken() ?: return null

        val refreshClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

        val refreshReq = Request.Builder()
            .url(RetrofitClient.BASE_URL + "api/auth/refresh")
            .addHeader("Refresh-Token", refreshToken)
            .post("".toRequestBody(null))
            .build()

        val refreshResp = refreshClient.newCall(refreshReq).execute()
        if (!refreshResp.isSuccessful) {

            return null
        }

        val bodyStr = refreshResp.body?.string().orEmpty()
        val data = JSONObject(bodyStr).getJSONObject("data")
        val newAccess  = data.getString("accessToken")
        val newRefresh = data.getString("refreshToken")

        TokenManager.saveAccessToken(newAccess)
        TokenManager.saveRefreshToken(newRefresh)

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun responseCount(resp: Response): Int =
        resp.priorResponse?.let { 1 + responseCount(it) } ?: 1
}


