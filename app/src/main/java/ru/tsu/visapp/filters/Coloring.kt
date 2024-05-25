package ru.tsu.visapp.filters

import android.graphics.Color
import kotlinx.coroutines.async
import androidx.core.graphics.red
import androidx.core.graphics.blue
import kotlinx.coroutines.awaitAll
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import kotlinx.coroutines.Dispatchers
import ru.tsu.visapp.utils.PixelsEditor
import kotlinx.coroutines.coroutineScope

class Coloring {
    data class ProcessPixel(
        val width: Int,
        val height: Int,
        val redValue: Int,
        val greenValue: Int,
        val blueValue: Int,
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val pixelsEditor: PixelsEditor,
        val resultPixelsEditor: PixelsEditor
    ) {
        fun start(i: Int, j: Int) {
            if (i in left..right && j in top..bottom) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                val alpha = pixel.alpha
                val red = (pixel.red + redValue).coerceIn(0, 255)
                val green = (pixel.green + greenValue).coerceIn(0, 255)
                val blue = (pixel.blue + blueValue).coerceIn(0, 255)

                resultPixelsEditor.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }
    }

    suspend fun coloring(
        pixels: IntArray,
        width: Int,
        height: Int,
        redValue: Int,
        greenValue: Int,
        blueValue: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): IntArray = coroutineScope {
        val resultPixels = pixels.copyOf()

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val resultPixelsEditor = PixelsEditor(resultPixels, width, height)

        val processPixel = ProcessPixel(
            width,
            height,
            redValue,
            greenValue,
            blueValue,
            left,
            top,
            right,
            bottom,
            pixelsEditor,
            resultPixelsEditor
        )

        val halfWidth = width / 2
        val halfHeight = height / 2

        val jobs = arrayOf(
            arrayOf(0, halfWidth, 0, halfHeight),
            arrayOf(halfWidth, width, 0, halfHeight),
            arrayOf(0, halfWidth, halfHeight, height),
            arrayOf(halfWidth, width, halfHeight, height)
        ).map { a ->
            async(Dispatchers.Default) {
                for (i in a[0] until a[1]) {
                    for (j in a[2] until a[3]) {
                        processPixel.start(i, j)
                    }
                }
            }
        }

        jobs.awaitAll()

        return@coroutineScope resultPixels
    }
}