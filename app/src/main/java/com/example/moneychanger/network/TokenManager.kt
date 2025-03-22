package com.example.moneychanger.network

import com.google.firebase.appdistribution.gradle.RefreshToken

object TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    // 액세스 토큰 저장
    fun saveAccessToken(token: String) {
        accessToken = token
    }

    // 액세스 토큰 가져오기
    fun getAccessToken(): String? {
        return accessToken
    }
    // 리프레시 토큰 저장
    fun saveRefreshToken(token: String) {
        refreshToken = token
    }

    // 리프레시 토큰 가져오기
    fun getRefreshToken(): String? {
        return refreshToken
    }
    // 모든 토큰 초기화  (로그아웃 시 사용)
    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}
