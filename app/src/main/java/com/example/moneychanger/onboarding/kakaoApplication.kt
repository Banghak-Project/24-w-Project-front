package com.example.moneychanger.onboarding

import android.app.Application
import android.util.Log
import com.example.moneychanger.R
import com.example.moneychanger.network.TokenManager
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class kakaoApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
        TokenManager.init(applicationContext)


        var keyHash = Utility.getKeyHash(this)
        Log.d("키 확인 : ", keyHash)
        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))

    }

}