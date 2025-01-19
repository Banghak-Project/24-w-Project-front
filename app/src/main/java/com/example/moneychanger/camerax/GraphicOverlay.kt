package com.example.moneychanger.camerax

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector
import kotlin.math.ceil

open class GraphicOverlay(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()
    var mScale: Float? = null
    var mOffsetX: Float? = null
    var mOffsetY: Float? = null
    var cameraSelector: Int = CameraSelector.LENS_FACING_BACK
    lateinit var processBitmap: Bitmap
    lateinit var processCanvas: Canvas

    abstract class Graphic(private val overlay: GraphicOverlay) {

        abstract fun draw(canvas: Canvas?)

        fun calculateRect(height: Float, width: Float, boundingBoxT: Rect): RectF {

            val scaleX = overlay.width.toFloat()/height
            val scaleY = overlay.height.toFloat()/width
            val scale = scaleX.coerceAtLeast(scaleY)
            overlay.mScale = scale

            // 화면에 이미지가 정중앙에 오도록 좌표 시작점 계산
            val offsetX = (overlay.width.toFloat() - ceil(height * scale)) / 2.0f
            val offsetY = (overlay.height.toFloat() - ceil(width * scale)) / 2.0f

            overlay.mOffsetX = offsetX
            overlay.mOffsetY = offsetY

            val mappedBox = RectF().apply {
                left = boundingBoxT.right * scale + offsetX
                top = boundingBoxT.top * scale + offsetY
                right = boundingBoxT.left * scale + offsetX
                bottom = boundingBoxT.bottom * scale + offsetY
            }

            return mappedBox
        }

        fun translateX(horizontal: Float): Float {
            return if (overlay.mScale != null && overlay.mOffsetX != null) {
                (horizontal * overlay.mScale!!) + overlay.mOffsetX!!
            } else {
                horizontal
            }
        }

        fun translateY(vertical: Float): Float {
            return if (overlay.mScale != null && overlay.mOffsetY != null) {
                (vertical * overlay.mScale!!) + overlay.mOffsetY!!
            } else {
                vertical
            }
        }

    }

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    private fun initProcessCanvas() {
        processBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        processCanvas = Canvas(processBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            initProcessCanvas()
            graphics.forEach {
                it.draw(canvas)
                it.draw(processCanvas)
            }
        }
    }
    fun setCameraInfo(imageWidth: Int, imageHeight: Int, isImageFlipped: Boolean) {
        synchronized(lock) {
            val viewAspectRatio = width.toFloat() / height
            val imageAspectRatio = imageWidth.toFloat() / imageHeight

            // 이미지 회전 및 비율에 따라 스케일 계산
            mScale = if (imageAspectRatio > viewAspectRatio) {
                width.toFloat() / imageWidth
            } else {
                height.toFloat() / imageHeight
            }

            // 좌표 중심 정렬
            mOffsetX = (width - mScale!! * imageWidth) / 2
            mOffsetY = (height - mScale!! * imageHeight) / 2

            // 카메라 플립 처리
            cameraSelector = if (isImageFlipped) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }

            postInvalidate() // 변경된 설정 적용
        }
    }

}

