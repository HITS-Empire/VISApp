package ru.tsu.visapp.utils

import java.io.File
import android.net.Uri
import android.view.View
import android.os.Environment
import android.graphics.Bitmap
import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.BitmapFactory
import androidx.fragment.app.Fragment
import android.content.ContentResolver
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

/*
 * Вспомогательные методы для работы с изображениями
 */

class ImageEditor {
    lateinit var contentResolver: ContentResolver

    private val albumName = "VISApp"
    private val albumRelativePath = "${Environment.DIRECTORY_PICTURES}/$albumName"
    private val albumFile = File(albumRelativePath)

    // Наложение одного bitmap на другой
    fun overlayBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap1.width, bitmap1.height, bitmap1.config)
        val canvas = Canvas(resultBitmap)

        val paint = Paint()

        // Наложение первого изображения
        canvas.drawBitmap(bitmap1, 0f, 0f, paint)

        // Наложение второго изображения с учетом прозрачности
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
        canvas.drawBitmap(bitmap2, 0f, 0f, paint)

        return resultBitmap
    }

    // Получить координаты картинки по нажатию на View
    fun getPointFromImageView(
        view: View,
        eventX: Float,
        eventY: Float,
        width: Int,
        height: Int
    ): IntArray? {
        val viewWidth = view.width.toFloat()
        val viewHeight = view.height.toFloat()
        val imageWidth = width.toFloat()
        val imageHeight = height.toFloat()

        val viewScale = viewWidth / viewHeight
        val imageScale = imageWidth / imageHeight

        val x: Float
        val y: Float

        if (viewScale < imageScale) {
            // Тёмные поля будут сверху и снизу
            val pixelScale = imageWidth / viewWidth

            val scaledImageHeight = imageHeight / pixelScale
            val indentY = (viewHeight - scaledImageHeight) / 2
            val scaledEventY = eventY - indentY

            if (scaledEventY < 0 || viewHeight < eventY + indentY) {
                return null
            }

            x = eventX * pixelScale
            y = scaledEventY * pixelScale
        } else {
            // Тёмные поля будут слева и справа
            val pixelScale = imageHeight / viewHeight

            val scaledImageWidth = imageWidth / pixelScale
            val indentX = (viewWidth - scaledImageWidth) / 2
            val scaledEventX = eventX - indentX

            if (scaledEventX < 0 || viewWidth < eventX + indentX) {
                return null
            }

            x = scaledEventX * pixelScale
            y = eventY * pixelScale
        }

        return intArrayOf(x.toInt(), y.toInt())
    }

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