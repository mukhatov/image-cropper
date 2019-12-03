package com.cropper.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

fun ImageView.createBitmap(x: Int, y: Int, newWidth: Int, newHeight: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return Bitmap.createBitmap(
            bitmap,
            x,
            y,
            newWidth,
            newHeight
    )
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            true
    )
}

fun ImageView.rotateBitmapByExif(uri: Uri): Bitmap? {
    context.contentResolver.openInputStream(uri)?.let {
        val img = BitmapFactory.decodeStream(it)
        val exifInterface = ExifInterface(it)
        val orientation: Float =
                when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
        return img.rotate(orientation)
    }
    return null
}

fun Bitmap.getCroppedFile(context: Context): File =
        File.createTempFile(
                "crop_${SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.US
                ).format(Date())}_",
                ".jpg",
                context.cacheDir
        ).apply {
            writeBitmap(
                    this@getCroppedFile,
                    Bitmap.CompressFormat.JPEG,
                    100
            )
        }