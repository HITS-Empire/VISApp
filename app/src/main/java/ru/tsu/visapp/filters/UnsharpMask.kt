package ru.tsu.visapp.filters

import android.graphics.Color
import kotlinx.coroutines.async
import androidx.core.graphics.red
import androidx.core.graphics.blue
import kotlinx.coroutines.awaitAll
import androidx.core.graphics.green
import androidx.core.graphics.alpha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import ru.tsu.visapp.utils.PixelsEditor

open class UnsharpMask {
    data class ProcessPixel(
        val width: Int,
        val height: Int,
        val radius: Int,
        val pixelsEditor: PixelsEditor,
        val resultEditor: PixelsEditor
    ) {
        fun start(i: Int, j: Int) {
            var totalRed = 0
            var totalGreen = 0
            var totalBlue = 0
            var count = 0

            // Цикл по окрестности пикселя с радиусом размытия
            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    val newX = i + x
                    val newY = j + y

                    if (newX in 0..<width && newY in 0..<height) {
                        val neighbor = pixelsEditor.getPixel(newX, newY) ?: 0
                        totalRed += neighbor.red
                        totalGreen += neighbor.green
                        totalBlue += neighbor.blue
                        count++
                    }
                }
            }

            val avgRed = totalRed / count
            val avgGreen = totalGreen / count
            val avgBlue = totalBlue / count

            val pixel: Int = pixelsEditor.getPixel(i, j) ?: 0

            resultEditor.setPixel(
                i,
                j,
                Color.argb(
                    pixel.alpha,
                    pixel.red - avgRed,
                    pixel.green - avgGreen,
                    pixel.blue - avgBlue
                )
            )
        }
    }

    private suspend fun gaussianBlur(
        pixels: IntArray,
        width: Int,
        height: Int,
        radius: Int
    ): IntArray = coroutineScope {
        val result = IntArray(pixels.size) { 0 }

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val resultEditor = PixelsEditor(result, width, height)

        val processPixel = ProcessPixel(
            width,
            height,
            radius,
            pixelsEditor,
            resultEditor
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

        return@coroutineScope result
    }

    // Преобразование цвета пикселя в яркость
    private fun luminancePercent(pixel: Int): Double {
        val luminance = (0.2126 * pixel.red + 0.7152 * pixel.green + 0.0722 * pixel.blue).toInt()

        // Нормализация яркости к процентам от максимальной яркости
        return luminance.toDouble() / 255.0 * 100.0
    }

    suspend fun usm(
        pixels: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        amountPercent: Int,
        threshold: Int
    ): IntArray = coroutineScope {
        val coefficient = amountPercent.toDouble() / 100

        // Создание копии изображения с применением размытия по Гауссу
        val result = gaussianBlur(pixels, width, height, radius)

        pixels.forEachIndexed { index, pixel ->
            val resultPixel = Color.argb(
                pixel.alpha,
                (pixel.red + (coefficient * result[index].red).toInt()).coerceIn(0, 255),
                (pixel.green + (coefficient * result[index].green).toInt()).coerceIn(0, 255),
                (pixel.blue + (coefficient * result[index].blue).toInt()).coerceIn(0, 255)
            )

            result[index] = if (
                luminancePercent(resultPixel) > threshold
            ) resultPixel else pixel
        }

        return@coroutineScope result
    }
}