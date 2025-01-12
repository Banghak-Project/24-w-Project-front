package com.example.moneychanger.mlkitOCR

import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.annotation.NonNull
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
object TextRecognitionWrapper {

    /**
     * 기존 `TextRecognition.getClient()` 메서드 래핑
     */
    @NonNull
    fun getClient(): TextRecognizer {
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * 새 `getCustomClient()` 메서드 추가
     * 필요하면 사용자 정의 로직 포함
     */
    @NonNull
    fun getCustomClient(): TextRecognizer {
        // 기본 구현 외에 커스텀 설정이 필요할 경우 구현
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
}
