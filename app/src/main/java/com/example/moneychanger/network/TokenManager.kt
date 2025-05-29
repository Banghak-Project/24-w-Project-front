package com.example.moneychanger.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.moneychanger.network.user.SignInResponse
import com.example.moneychanger.network.user.UserInfoResponse
import com.google.gson.Gson

object TokenManager {
    private const val PREF_NAME = "MoneyChangerPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_SIGN_IN_INFO = "sign_in_info"
    private const val KEY_USER_INFO = "user_info"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_KAKAO_USER = "isKakaoUser"
    private const val KEY_IS_GOOGLE_USER = "isGoogleUser"



    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(token: String) {
        Log.d("TokenManager", "✅ 저장 시도 accessToken = $token")
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        Log.d("TokenManager", "✅ 저장 후 실제 값 = ${getAccessToken()}")
    }


    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun saveSignInInfo(signInResponse: SignInResponse) {
        val json = Gson().toJson(signInResponse)
        prefs.edit().putString(KEY_SIGN_IN_INFO, json).apply()
    }

    fun saveUserInfo(userInfo: UserInfoResponse?) {
        if (userInfo != null) {
            val json = Gson().toJson(userInfo)
            prefs.edit()
                .putString(KEY_USER_INFO, json)
                .putBoolean(KEY_IS_KAKAO_USER, userInfo.isKakaoUser ?: false)
                .putBoolean(KEY_IS_GOOGLE_USER, userInfo.isGoogleUser ?: false)
                .apply()
        }
    }



    fun getUserInfo(): UserInfoResponse? {
        val json = prefs.getString(KEY_USER_INFO, null) ?: return null
        return Gson().fromJson(json, UserInfoResponse::class.java)
    }

    fun saveUserId(id: Long) {
        prefs.edit().putLong(KEY_USER_ID, id).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
    fun isGoogleUser(): Boolean {
        return prefs.getBoolean(KEY_IS_GOOGLE_USER, false)
    }
    fun isKakaoUser(): Boolean {
        return prefs.getBoolean(KEY_IS_KAKAO_USER, false)
    }

}
