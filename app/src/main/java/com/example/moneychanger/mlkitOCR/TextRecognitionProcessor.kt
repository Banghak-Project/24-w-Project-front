package com.example.moneychanger.mlkitOCR

import android.graphics.Rect
import android.util.Log
import com.example.moneychanger.camerax.BaseImageAnalyzer
import com.example.moneychanger.camerax.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import java.io.IOException

class TextRecognitionProcessor(
    private val view:GraphicOverlay,
    private val onTextDetected: (String)-> Unit
) : BaseImageAnalyzer<Text>() {

    private val recognizer = TextRecognitionWrapper.getClient()
    override val graphicOverlay: GraphicOverlay
        get() = view

    //MLKit TextRecognition API를 이용해 입력 이미지를 처리
    override fun detectInImage(image: InputImage): Task<Text> {
        return recognizer.process(image)
    }

    override fun stop() {
        try {
            recognizer.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Recognition: $e")
        }
    }

    override fun onSuccess(
        results: Text,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    ) {
        graphicOverlay.clear()
        val imageWidth = rect.width()
        val imageHeight = rect.height()
        graphicOverlay.setCameraInfo(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            isImageFlipped = false
        )
        val allText = StringBuilder()
        results.textBlocks.forEach {
            allText.append(it.text).append("\n")
            val textGraphic = TextRecognitionGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(textGraphic)
        }
        graphicOverlay.postInvalidate()
        onTextDetected(allText.toString()) // 텍스트 전달
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Text Recognition failed.$e")
    }

    companion object {
        private const val TAG = "TextProcessor"
    }
}
