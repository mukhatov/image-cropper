package com.cropper

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_cropper.*

class CropperActivity : AppCompatActivity() {

    companion object {
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 10000
        const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 20000
        const val GALLERY_REQUEST_CODE = 30000
        const val CAMERA_REQUEST_CODE = 40000
    }

    private var menuItemDone: MenuItem? = null

    private var photoPath: String? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cropper, menu)
        menuItemDone = menu?.findItem(R.id.action_done)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropper)
        supportActionBar?.setTitle(R.string.title_move_and_scale)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setImageUri(uri: Uri) {
        cropperLayout.setImageUri(uri)
        menuItemDone?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when (item.itemId) {
            android.R.id.home -> {
                setResult(RESULT_CANCELED)
                finish()
                true
            }
            R.id.action_gallery -> {
                onChooseImage()
                true
            }
            R.id.action_camera -> {
                onTakePhoto()
                true
            }
            R.id.action_done -> {
                val intent = Intent()
                intent.data = Uri.parse(cropperLayout.getCroppedFile().absolutePath)
                setResult(RESULT_OK, intent)
                finish()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        })
    }

    private fun onChooseImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(packageManager) == null) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
            }
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        }
    }

    private fun onTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            val fileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.resolveActivity(packageManager)?.also {
                photoPath = fileUri.toString()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onChooseImage()
                }
            }
            WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onTakePhoto()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let {
                        setImageUri(it)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    photoPath?.let {
                        setImageUri(Uri.parse(photoPath))
                    }
                }
            }
        }
    }
}