package com.example.moneychanger.network

import android.content.Context
import android.content.SharedPreferences
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

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
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

    // ✅ 회원 정보 저장 (null 방지)
    fun saveUserInfo(userInfo: UserInfoResponse?) {
        if (userInfo != null) {
            val json = Gson().toJson(userInfo)
            prefs.edit().putString(KEY_USER_INFO, json).apply()
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

    // ✅ 전체 사용자 정보 업데이트 (null 방지)
    fun updateUserInfo(newInfo: UserInfoResponse?) {
        saveUserInfo(newInfo)
    }
}
