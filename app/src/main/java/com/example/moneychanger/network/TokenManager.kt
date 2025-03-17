package com.example.moneychanger.network

import android.content.Context
import android.content.SharedPreferences
import com.example.moneychanger.network.user.SignInResponse
import com.google.gson.Gson

object TokenManager {
    private const val PREF_NAME = "MoneyChangerPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_INFO = "user_info"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ✅ 액세스 토큰 저장
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    // ✅ 액세스 토큰 가져오기
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // ✅ 리프레시 토큰 저장
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    // ✅ 리프레시 토큰 가져오기
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // ✅ 사용자 정보 저장
    fun saveUserInfo(userInfo: SignInResponse) {
        val json = Gson().toJson(userInfo)
        prefs.edit().putString(KEY_USER_INFO, json).apply()
    }

    // ✅ 사용자 정보 가져오기 (이메일도 포함됨)
    fun getUserInfo(): SignInResponse? {
        val json = prefs.getString(KEY_USER_INFO, null) ?: return null
        return Gson().fromJson(json, SignInResponse::class.java)
    }

    // ✅ 모든 토큰 및 사용자 정보 삭제 (로그아웃 시 사용)
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
