package com.example.moneychanger.network

object TokenManager {
    private var accessToken: String? = null

    fun saveAccessToken(token: String) {
        accessToken = token
    }

    fun getAccessToken(): String? {
        return accessToken
    }

    fun clearTokens() {
        accessToken = null
    }
}
