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

    // ✅ 로그인 정보 저장 (SignInResponse)
    fun saveSignInInfo(signInResponse: SignInResponse) {
        val json = Gson().toJson(signInResponse)
        prefs.edit().putString(KEY_SIGN_IN_INFO, json).apply()
    }

    // ✅ 로그인 정보 가져오기
    fun getSignInInfo(): SignInResponse? {
        val json = prefs.getString(KEY_SIGN_IN_INFO, null) ?: return null
        return Gson().fromJson(json, SignInResponse::class.java)
    }

    // ✅ 회원 정보 저장 (UserInfoResponse)
    fun saveUserInfo(userInfo: UserInfoResponse) {
        val json = Gson().toJson(userInfo)
        prefs.edit().putString(KEY_USER_INFO, json).apply()
    }

    // ✅ 회원 정보 가져오기
    fun getUserInfo(): UserInfoResponse? {
        val json = prefs.getString(KEY_USER_INFO, null) ?: return null
        return Gson().fromJson(json, UserInfoResponse::class.java)
    }

    // ✅ 유저 ID 저장
    fun saveUserId(id: Long) {
        prefs.edit().putLong(KEY_USER_ID, id).apply()
    }

    // ✅ 유저 ID 가져오기
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1) // 기본값 -1 (존재하지 않으면)
    }

    // ✅ 모든 토큰 및 정보 삭제 (로그아웃 시 사용)
    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    // ✅ 사용자 이름 업데이트
    fun updateUserName(newName: String) {
        val userInfo = getUserInfo()
        if (userInfo != null) {
            val updatedUser = userInfo.copy(userName = newName)
            saveUserInfo(updatedUser)
        }
    }

    // ✅ 생년월일 업데이트
    fun updateUserBirth(newBirth: String?) {
        val userInfo = getUserInfo()
        if (userInfo != null) {
            val updatedBirth = newBirth ?: userInfo.userDateOfBirth // 기존 값 유지
            val updatedUser = userInfo.copy(userDateOfBirth = updatedBirth)
            saveUserInfo(updatedUser)
        }
    }
}
