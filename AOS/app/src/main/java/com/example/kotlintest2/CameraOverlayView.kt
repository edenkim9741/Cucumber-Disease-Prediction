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

    private val cornerRadius = 27f * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // 전체 어두운 배경
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // 가운데 투명한 둥근 사각형
        val frameWidth = 340 * resources.displayMetrics.density
        val frameHeight = 448 * resources.displayMetrics.density
        val left = (width - frameWidth) / 2
        val top = (height - frameHeight) / 2
        val right = left + frameWidth
        val bottom = top + frameHeight

        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, clearPaint)

        canvas.restoreToCount(saveCount)
    }
}