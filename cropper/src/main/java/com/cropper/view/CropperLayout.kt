package com.cropper.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.cropper.R
import com.cropper.common.writeBitmap
import kotlinx.android.synthetic.main.view_cropper.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CropperLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) :
    FrameLayout(
        context,
        attrs,
        defStyleAttr
    ) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_cropper, this)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CropperLayout)
        setBackgroundColor(typedArray.getColor(R.styleable.CropperLayout_backgroundColor, Color.BLACK))
        val padding =
            typedArray.getDimension(R.styleable.CropperLayout_padding, resources.getDimension(R.dimen.default_padding))
                .toInt()
        cropperImageView.horizontalPadding = padding
        cropperImageView.maxScale = typedArray.getFloat(R.styleable.CropperLayout_maxScale, 4f)
        overlayView.overlayColor = typedArray.getColor(R.styleable.CropperLayout_overlayColor, Color.BLACK)
        overlayView.overlayAlpha = typedArray.getInt(R.styleable.CropperLayout_overlayAlpha, 140)
        overlayView.borderColor = typedArray.getColor(R.styleable.CropperLayout_borderColor, Color.WHITE)
        overlayView.borderWidth = typedArray.getDimension(
            R.styleable.CropperLayout_borderWidth,
            resources.getDimension(R.dimen.default_border_width)
        )
        overlayView.borderEnable = typedArray.getBoolean(R.styleable.CropperLayout_borderEnable, false)
        overlayView.mode = typedArray.getInt(R.styleable.CropperLayout_mode, 0)
        overlayView.padding = padding
        typedArray.recycle()
    }

    fun setImageUri(uri: Uri) =
        cropperImageView.setImageURI(uri)

    fun crop(): Bitmap =
        cropperImageView.crop()

    fun getCroppedImage(): File =
        File.createTempFile(
            "crop_${SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(Date())}_",
            ".jpg",
            context.cacheDir
        ).apply {
            writeBitmap(
                crop(),
                Bitmap.CompressFormat.JPEG,
                100
            )
        }
}