package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.alpha
import ru.tsu.visapp.utils.PixelsEditor

open class UnsharpMask {
    private fun gaussianBlur(
        pixels: IntArray,
        width: Int,
        height: Int,
        radius: Int
    ): IntArray {
        val result = IntArray(pixels.size) { 0 }

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val resultEditor = PixelsEditor(result, width, height)

        // Основной цикл по изображению
        for (i in 0..<width) {
            for (j in 0..<height) {
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

        return result
    }

    // Преобразование цвета пикселя в яркость
    private fun luminancePercent(pixel: Int): Double {
        val luminance = (0.2126 * pixel.red + 0.7152 * pixel.green + 0.0722 * pixel.blue).toInt()

        // Нормализация яркости к процентам от максимальной яркости
        return luminance.toDouble() / 255.0 * 100.0
    }

    fun usm(
        pixels: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        amountPercent: Int,
        threshold: Int
    ): IntArray {
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

        return result
    }
}