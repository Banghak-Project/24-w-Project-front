package com.example.moneychanger.onboarding

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class kakaoApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "6bd868dc4dfc4411b3ed036307340a65")

    }

}