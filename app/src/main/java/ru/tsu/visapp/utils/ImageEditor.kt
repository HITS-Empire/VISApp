package ru.tsu.visapp.utils

import java.io.File
import android.net.Uri
import android.os.Environment
import android.graphics.Bitmap
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.content.ContentResolver

/*
 * Вспомогательные методы для работы с изображениями
 */

class ImageEditor(initContentResolver: ContentResolver) {
    private val contentResolver = initContentResolver

    private val albumName = "VISApp"
    private val albumRelativePath = "${Environment.DIRECTORY_PICTURES}/$albumName"
    private val albumFile = File(albumRelativePath)

    // Создать Bitmap по URI картинки
    fun createBitmapByURI(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val staticBitmap = BitmapFactory.decodeStream(inputStream)

        return staticBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    // Получить пиксели изображения
    fun getPixelsFromBitmap(bitmap: Bitmap): IntArray {
        val pixels = IntArray(bitmap.width * bitmap.height)

        bitmap.getPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )

        return pixels
    }

    // Установить пиксели в изображение
    fun setPixelsToBitmap(bitmap: Bitmap, pixels: IntArray) {
        bitmap.setPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
    }

    // Сохранить картинку в галерею
    fun saveImageToGallery(bitmap: Bitmap, title: String) {
        verifyAlbum()

        val name = "$title.jpg"
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