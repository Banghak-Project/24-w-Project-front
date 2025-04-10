package com.example.moneychanger.network

import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.currency.CurrencyResponseDto
import com.example.moneychanger.network.list.CreateListRequestDto
import com.example.moneychanger.network.list.CreateListResponseDto
import com.example.moneychanger.network.list.CreateListWithNameRequestDto
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.ListsResponseDto
import com.example.moneychanger.network.list.UpdateRequestDto
import com.example.moneychanger.network.list.UpdateResponseDto
import com.example.moneychanger.network.location.GeocodeResponse
import com.example.moneychanger.network.product.CreateProductRequestDto
import com.example.moneychanger.network.product.CreateProductResponseDto
import com.example.moneychanger.network.product.DeleteProductsRequestDto
import com.example.moneychanger.network.product.ImageProductResponseDto
import com.example.moneychanger.network.product.ProductModel
import com.example.moneychanger.network.product.ProductResponseDto
import com.example.moneychanger.network.product.UpdateProductRequestDto
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.network.user.EmailRequest
import com.example.moneychanger.network.user.FindPasswordRequest
import com.example.moneychanger.network.user.KakaoLoginRequest
import com.example.moneychanger.network.user.OtpRequest
import com.example.moneychanger.network.user.SignInRequest
import com.example.moneychanger.network.user.SignInResponse
import com.example.moneychanger.network.user.SignUpRequest
import com.example.moneychanger.network.user.SignUpResponse
import com.example.moneychanger.network.user.UpdateUserInfoRequest
import com.example.moneychanger.network.user.UserInfoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    //User
    @POST("/api/auth/signin")
    suspend fun signIn(@Body request: SignInRequest): Response<ApiResponse<SignInResponse>>

    @POST("/api/auth/signup")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): Response<ApiResponse<SignUpResponse>>

    @POST("/api/auth/signup/otp")
    suspend fun sendOtp(@Body emailRequest: EmailRequest): Response<Void>

    @POST("/api/auth/signup/otp/check")
    suspend fun verifyOtp(@Body request: OtpRequest): Response<ResponseBody>

    @POST("/api/auth/kakao/signin")
    suspend fun kakaoSignIn(@Body request: KakaoLoginRequest): Response<ApiResponse<SignInResponse>>

    //List
    @POST("/api/lists/add")
    fun createList(@Body requestDto: CreateListRequestDto): Call<ApiResponse<CreateListResponseDto>>

    @POST("/api/lists/add/name")
    fun createListWithName(@Body requestDto: CreateListWithNameRequestDto): Call<ApiResponse<CreateListResponseDto>>

    @GET("/api/lists")
    fun getAllLists(): Call<ApiResponse<List<ListsResponseDto>>>

    @GET("/api/lists/{id}")
    fun getListsById(@Path("id") id:Long): Call<ApiResponse<ListsResponseDto?>>

    @PATCH("/api/lists/delete/{id}")
    fun deleteList(@Path("id") id: Long): Call<ApiResponse<Void>>

    @PATCH("/api/lists/update")
    fun updateList(@Body requestDto: UpdateRequestDto): Call<ApiResponse<UpdateResponseDto>>

    //총금액표시
    @GET("/api/lists/total/{id}")
    fun getTotal(@Path("id") id: Long): Call<ApiResponse<Double>>

    //Product
    @POST("/api/products")
    fun createProduct(@Body requestDto: CreateProductRequestDto): Call<ApiResponse<CreateProductResponseDto>>

    //아이디에 맞는 상품 갖고옴
    @GET("/api/products/{id}")
    fun getProductByListsId(@Path("id") productId:Long): Call<ApiResponse<List<ProductResponseDto>>>
    //모든 상품 조회
    @GET("/api/products")
    fun getAllProducts(): Response<ApiResponse<Call<List<ProductModel>>>>

    //아이디에 맞는 상품 수정
    @PATCH("/api/products/update")
    fun updateProduct(
        @Body requestDto: UpdateProductRequestDto
    ): Call<ApiResponse<ProductResponseDto>>

    //아이디에 맞는 상품 삭제
    @PATCH("/api/products/{id}")
    fun deleteProduct(@Path("id") productId: Long): Call<ApiResponse<Void>>

    @PATCH("/api/products/selected")
    fun deleteProductsByIds(@Body request: DeleteProductsRequestDto): Call<ApiResponse<List<ProductResponseDto>>>


    //이미지 분석
    @Multipart
    @POST("/api/products/image")
    fun analyzeImage(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<ApiResponse<List<ImageProductResponseDto>>>

    //Notice
    //추후에 추가

    @POST("/api/currency/import")
    fun importCurrency(): Response<ApiResponse<Call<List<CurrencyModel>>>>

    @GET("/api/currency")
    fun findAll(): Call<ApiResponse<List<CurrencyResponseDto>>>

    // ID 찾기 API
    @GET("/api/auth/find-id")
    suspend fun findId(
        @Query("userName") userName: String,
        @Query("userDateOfBirth") userDateOfBirth: String
    ): Response<ApiResponse<String>>

    // 비밀번호 찾기 API (임시 비밀번호 발급)
    @POST("/api/auth/find-password")
    suspend fun findPassword(@Body request: FindPasswordRequest): Response<ApiResponse<String>>

    //  회원정보 조회
    @GET("/api/auth/user-info")
    suspend fun getUserInfo(): Response<ApiResponse<UserInfoResponse>>


    //  회원정보 수정
    @POST("/api/auth/update-user-info")
    suspend fun updateUserInfo(
        @Header("Authorization") token: String,
        @Body request: UpdateUserInfoRequest
    ): Response<ApiResponse<UserInfoResponse>>

    // google geocoding api
    interface GoogleGeocodingApi {
        @GET("geocode/json")
        suspend fun reverseGeocode(
            @Query("latlng") latlng: String,
            @Query("key") apiKey: String
        ): GeocodeResponse
    }
    // 회원탈퇴
    @POST("/api/auth/withdrawal")
    suspend fun withdrawal(
        @Body request: Map<String, String>
    ): Response<ApiResponse<String>>


    // 새 비밀번호 설정
    @POST("/api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: Map<String, String>
    ): Response<ApiResponse<String>>
}