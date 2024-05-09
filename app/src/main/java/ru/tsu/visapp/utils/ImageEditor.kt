package ru.tsu.visapp.utils

import java.io.File
import android.net.Uri
import android.os.Environment
import android.graphics.Bitmap
import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.BitmapFactory
import androidx.fragment.app.Fragment
import android.content.ContentResolver
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

/*
 * Вспомогательные методы для работы с изображениями
 */

class ImageEditor {
    lateinit var contentResolver: ContentResolver

    private val albumName = "VISApp"
    private val albumRelativePath = "${Environment.DIRECTORY_PICTURES}/$albumName"
    private val albumFile = File(albumRelativePath)

    // Очистить bitmap
    fun clearBitmap(bitmap: Bitmap) {
        val pixels = getPixelsFromBitmap(bitmap)

        pixels.forEachIndexed{index, _ ->
            run {
                pixels[index] = 0x000000FF.toInt()
            }
        }

        setPixelsToBitmap(bitmap, pixels)
    }

    // Получить URI сохранённой картинки
    fun getSavedImageUri(activity: AppCompatActivity?, fragment: Fragment?): Uri {
        var sharedPreferences: SharedPreferences? = null

        if (activity != null) {
            sharedPreferences = activity.getSharedPreferences(
                "ru.tsu.visapp",
                Context.MODE_PRIVATE
            )
        }
        if (fragment != null) {
            sharedPreferences = fragment.requireContext().getSharedPreferences(
                "ru.tsu.visapp",
                Context.MODE_PRIVATE
            )
        }

        val savedImageUriString = sharedPreferences?.getString("selected_uri", "")

        return Uri.parse(savedImageUriString)
    }

    // Создать Bitmap по URI картинки
    fun createBitmapByUri(uri: Uri): Bitmap {
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