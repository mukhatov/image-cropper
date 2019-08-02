package com.cropper.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private var mBitmap: Bitmap? = null

    var overlayColor: Int = Color.BLACK
    var alpha: Int = 140
    var mode: Int = 0
    var padding: Int = 20

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (mBitmap == null) {
            createWindowFrame()
        }
        mBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    private fun createWindowFrame() {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mBitmap?.let {
            val osCanvas = Canvas(it)

            val outerRectangle = RectF(0f, 0f, width.toFloat(), height.toFloat())

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = overlayColor
            paint.alpha = alpha
            osCanvas.drawRect(outerRectangle, paint)

            paint.color = Color.TRANSPARENT
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            val centerX = width / 2f
            val centerY = height / 2f
            if (mode == 0) {
                osCanvas.drawCircle(centerX, centerY, (width - 2 * padding) / 2f, paint)
            } else {
                val mWidth = (width - 2 * padding).toFloat()
                val rect =
                    Rect(padding, (centerY - mWidth / 2).toInt(), width - padding, (centerY + mWidth / 2).toInt())
                osCanvas.drawRect(rect, paint)
            }
        }
    }

    override fun isInEditMode(): Boolean {
        return true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        mBitmap = null
    }
}