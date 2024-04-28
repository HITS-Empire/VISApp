package ru.tsu.visapp.utils

import java.io.File
import android.os.Environment
import android.graphics.Bitmap
import android.provider.MediaStore
import android.content.ContentValues
import android.content.ContentResolver

class Editor(initContentResolver: ContentResolver) {
    private val contentResolver = initContentResolver

    private val albumName = "VISApp"
    private val albumRelativePath = "${Environment.DIRECTORY_PICTURES}/$albumName"
    private val albumFile = File(albumRelativePath)

    // Сохранить картинку в галерею
    fun saveImageToGallery(bitmap: Bitmap) {
        verifyAlbum()

        val name = "${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, albumRelativePath)
        }
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        val outputStream = contentResolver.openOutputStream(uri!!)!!

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    // Проверить, доступен ли альбом, и если недоступен, то создать
    private fun verifyAlbum() {
        if (!albumFile.exists()) albumFile.mkdir()
    }
}