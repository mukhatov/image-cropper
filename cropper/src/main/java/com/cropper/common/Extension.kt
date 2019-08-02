package com.cropper.common

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.ImageView
import java.io.File


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
    val path = uri.getImagePath(context)
    path?.let {
        val img = BitmapFactory.decodeFile(path)
        val exifInterface = ExifInterface(path)
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

fun Uri.getImagePath(context: Context): String? {
    if (DocumentsContract.isDocumentUri(context, this)) {
        val docId = DocumentsContract.getDocumentId(this)
        val split = docId.split(":".toRegex()).dropLastWhile {
            it.isEmpty()
        }.toTypedArray()

        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.let {
            getDataColumn(context, it, selection, selectionArgs)
        }

    } else if ("content".equals(this.scheme, ignoreCase = true)) {
        return getDataColumn(context, this, null, null)
    } else if ("file".equals(this.scheme, ignoreCase = true)) {
        return this.path
    }
    return null
}

private fun getDataColumn(
    context: Context, uri: Uri, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(column_index)
        }
    } finally {
        if (cursor != null)
            cursor.close()
    }
    return null
}