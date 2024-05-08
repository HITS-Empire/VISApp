package ru.tsu.visapp.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.File


/*
 * Вспомогательные методы для работы с изображениями
 */

class ImageEditor(initContentResolver: ContentResolver) {
    private val contentResolver = initContentResolver

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
    data class Pixel(val red: Int, val green: Int, val blue: Int) {
        operator fun minus(other: Pixel) : Pixel {
            return Pixel(
                this.red - other.red,
                this.green - other.green,
                this.blue - other.blue
            )
        }

        operator fun plus(other: Pixel) : Pixel {
            return Pixel(
                this.red + other.red,
                this.green + other.green,
                this.blue + other.blue
            )
        }

        operator fun times(percent: Double) : Pixel {
            return Pixel(
                (red * percent).toInt(),
                (green * percent).toInt(),
                (blue * percent).toInt()
            )
        }

        fun equals(digit: Int): Boolean {
            return (red == digit && blue == digit && green == digit)
        }

        fun notEquals(digit: Int): Boolean {
            return (red != digit && blue != digit && green != digit)
        }
    }

    fun bitmapToPixels(bitmap: Bitmap) : Array<Array<Pixel>> {
        val width: Int = bitmap.getWidth()
        val height: Int = bitmap.getHeight()

        var pixels = Array(bitmap.width) { Array(bitmap.height) { Pixel(0, 0, 0) } }

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixelColor = bitmap.getPixel(x, y)
                val red = Color.red(pixelColor)
                val green = Color.green(pixelColor)
                val blue = Color.blue(pixelColor)

                pixels[x][y] = Pixel(red, green, blue)
            }
        }

        return pixels
    }

    fun pixelsToBitmap(pixels: Array<Array<Pixel>>): Bitmap {
        val width = pixels.size
        val height = pixels[0].size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = pixels[x][y]
                val color = Color.rgb(pixel.red, pixel.green, pixel.blue)
                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
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