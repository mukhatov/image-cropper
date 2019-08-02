package com.cropper.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import com.cropper.common.createBitmap
import com.cropper.common.rotateBitmapByExif
import kotlin.math.max

class CropperImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs),
    ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

    private var initScale = 1f

    private val scaleGestureDetector: ScaleGestureDetector
    private val matrixValues = FloatArray(9)

    private var scaleMatrix = Matrix()

    var maxScale: Float = 4f
    var horizontalPadding: Int = 0
    private var verticalPadding: Int = 0

    private val scale: Float
        get() {
            scaleMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MSCALE_X]
        }

    private val matrixRectF: RectF
        get() {
            val matrix = scaleMatrix
            val rectF = RectF()
            val d = drawable
            if (null != d) {
                rectF.set(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
                matrix.mapRect(rectF)
            }
            return rectF
        }

    private var lastPointerCount: Int = 0
    private var mLastX: Float = 0f
    private var mLastY: Float = 0f

    init {
        scaleType = ScaleType.MATRIX
        scaleGestureDetector = ScaleGestureDetector(context, this)
        setOnTouchListener(this)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        initialization()
    }

    override fun setImageURI(uri: Uri?) {
        uri?.let {
            val bitmap = rotateBitmapByExif(it)
            if (bitmap != null) {
                setImageBitmap(bitmap)
            } else {
                super.setImageURI(uri)
                initialization()
            }
        }
    }

    private fun initialization() {
        scaleMatrix = Matrix()

        val d = drawable ?: return
        val width = width
        val height = height
        val dw = d.intrinsicWidth
        val dh = d.intrinsicHeight
        verticalPadding = (height - (width - horizontalPadding * 2)) / 2

        val wScale = (width - horizontalPadding * 2f) / dw
        val hScale = (height - verticalPadding * 2f) / dh
        val scale = max(wScale, hScale)

        initScale = scale
        scaleMatrix.postTranslate(((width - dw) / 2f), ((height - dh) / 2f))
        scaleMatrix.postScale(scale, scale, (width / 2f), (height / 2f))
        imageMatrix = scaleMatrix
    }

    fun crop(): Bitmap =
        createBitmap(
            horizontalPadding,
            verticalPadding,
            width - 2 * horizontalPadding,
            width - 2 * horizontalPadding
        )

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (drawable == null)
            return true
        val scale = scale
        var scaleFactor = detector.scaleFactor

        if (scale < maxScale && scaleFactor > 1f || scale > initScale && scaleFactor < 1f) {
            if (scaleFactor * scale < initScale) {
                scaleFactor = initScale / scale
            }
            if (scaleFactor * scale > maxScale) {
                scaleFactor = maxScale / scale
            }

            scaleMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            checkRectWhenScale()
            imageMatrix = scaleMatrix
        }

        return true
    }

    private fun checkRectWhenScale() {
        val rect = matrixRectF
        var deltaX = 0f
        var deltaY = 0f

        val width = width
        val height = height
        if (rect.width() > width) {
            if (rect.left > 0) {
                deltaX = -rect.left
            }
            if (rect.right < width) {
                deltaX = width - rect.right
            }
        }
        if (rect.height() > height) {
            if (rect.top > 0) {
                deltaY = -rect.top
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom
            }
        }

        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + rect.width() * 0.5f
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + rect.height() * 0.5f
        }
        scaleMatrix.postTranslate(deltaX, deltaY)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {

    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        var x = 0f
        var y = 0f
        val pointerCount = event.pointerCount
        for (i in 0 until pointerCount) {
            x += event.getX(i)
            y += event.getY(i)
        }
        x /= pointerCount
        y /= pointerCount

        if (pointerCount != lastPointerCount) {
            mLastX = x
            mLastY = y
        }
        lastPointerCount = pointerCount

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - mLastX
                val dy = y - mLastY

                scaleMatrix.postTranslate(dx, dy)
                imageMatrix = scaleMatrix
                mLastX = x
                mLastY = y
            }
            MotionEvent.ACTION_UP -> {
                checkBorder()
                imageMatrix = scaleMatrix

                lastPointerCount = 0
            }
            MotionEvent.ACTION_CANCEL -> {
                lastPointerCount = 0
            }
        }

        return true
    }

    private fun checkBorder() {

        val rect = matrixRectF
        var deltaX = 0f
        var deltaY = 0f

        val width = width
        val height = height

        if (rect.width() >= width - 2 * horizontalPadding) {
            if (rect.left > horizontalPadding) {
                deltaX = -rect.left + horizontalPadding
            }
            if (rect.right < width - horizontalPadding) {
                deltaX = width.toFloat() - horizontalPadding.toFloat() - rect.right
            }
        }
        if (rect.height() >= height - 2 * verticalPadding) {
            if (rect.top > verticalPadding) {
                deltaY = -rect.top + verticalPadding
            }
            if (rect.bottom < height - verticalPadding) {
                deltaY = height.toFloat() - verticalPadding.toFloat() - rect.bottom
            }
        }
        scaleMatrix.postTranslate(deltaX, deltaY)

    }
}