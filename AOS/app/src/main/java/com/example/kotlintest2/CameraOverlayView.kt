package com.example.kotlintest2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#AA000000")
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var frameRect: RectF? = null // 투명 영역 위치 저장
    private var frameCornerRadius = 0f

    // CameraFragment에서 호출
    fun setFrameRect(rect: RectF, cornerRadius: Float) {
        this.frameRect = rect
        this.frameCornerRadius = cornerRadius
        invalidate() // "다시 그려!" 명령
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // ① 전체를 검은색 반투명으로 칠함
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // ② frameRect 위치만 투명하게 뚫음
        frameRect?.let { rect ->
            canvas.drawRoundRect(rect, frameCornerRadius, frameCornerRadius, clearPaint)
        }

        canvas.restoreToCount(saveCount)
    }
}