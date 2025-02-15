package com.example.moneychanger.network

import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.ListsRequestDto
import com.example.moneychanger.network.list.ListsResponseDto
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductRequestDto
import com.example.moneychanger.network.user.EmailRequest
import com.example.moneychanger.network.user.KakaoLoginRequest
import com.example.moneychanger.network.user.KakaoLoginResponse
import com.example.moneychanger.network.user.OtpRequest
import com.example.moneychanger.network.user.SignInRequest
import com.example.moneychanger.network.user.SignInResponse
import com.example.moneychanger.network.user.SignUpRequest
import com.example.moneychanger.network.user.SignUpResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    //User
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

    //List
    @POST("/api/lists/add")
    fun createList(@Body requestDto: ListsRequestDto): Call<ListModel>

    @GET("/api/lists")
    fun getAllLists(): Call<List<ListsResponseDto>>

    @DELETE("/api/lists/delete/{id}")
    fun deleteList(@Path("id") id: Long): Call<Void>

    //Product
    @POST("/api/products")
    fun createProduct(@Body requestDto: ProductRequestDto): Call<ProductModel>
    //아이디에 맞는 상품 갖고옴
    @GET("/api/products/{id}")
    fun getProductByListsId(@Path("id") productId:Long): Call<ProductModel>
    //모든 상품 조회
    @GET("/api/products")
    fun getAllProducts(): Call<List<ProductModel>>
    //아이디에 맞는 상품 수정
    @PUT("/api/products/{id}")
    fun updateProduct(@Path("id") productId: Long, @Body requestDto: ProductRequestDto): Call<ProductModel>
    //아이디에 맞는 상품 삭제
    @DELETE("/api/products/{id}")
    fun deleteProduct(@Path("id") productId: Long): Call<Void>

    //Notice
    //추후에 추가

    //Currency
    @POST("/api/currency/import")
    fun importCurrency(): Call<List<CurrencyModel>>

    @GET("/api/currency")
    fun findAll(): Call<List<CurrencyModel>>

}