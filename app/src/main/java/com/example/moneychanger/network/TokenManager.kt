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

    fun getAccessTokenWithBearer(): String? {
        val token = getAccessToken()
        return if (token != null && !token.startsWith("Bearer ")) {
            "Bearer $token"
        } else {
            token
        }
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveSignInInfo(signInResponse: SignInResponse) {
        val json = Gson().toJson(signInResponse)
        prefs.edit().putString(KEY_SIGN_IN_INFO, json).apply()
    }

    // ✅ 기존 함수 기반으로 수정 + isKakaoUser도 저장
    fun saveUserInfo(userInfo: UserInfoResponse?) {
        if (userInfo != null) {
            val json = Gson().toJson(userInfo)
            prefs.edit().putString(KEY_USER_INFO, json).apply()

            // ✅ 카카오 유저 여부 저장
            prefs.edit().putBoolean(KEY_IS_KAKAO_USER, userInfo.isKakaoUser ?: false).apply()
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

    fun updateUserName(newName: String) {
        val userInfo = getUserInfo()
        if (userInfo != null) {
            val updatedUser = userInfo.copy(userName = newName)
            saveUserInfo(updatedUser)
        }
    }

    fun updateUserBirth(newBirth: String?) {
        val userInfo = getUserInfo()
        if (userInfo != null) {
            val updatedBirth = newBirth ?: userInfo.userDateOfBirth
            val updatedUser = userInfo.copy(userDateOfBirth = updatedBirth)
            saveUserInfo(updatedUser)
        }
    }

    fun updateUserInfo(newInfo: UserInfoResponse?) {
        saveUserInfo(newInfo)
    }

    // ✅ 저장된 isKakaoUser 여부 확인 (null 방지)
    fun isKakaoUser(): Boolean {
        return prefs.getBoolean(KEY_IS_KAKAO_USER, false)
    }

}
