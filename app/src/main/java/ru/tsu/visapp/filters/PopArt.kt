package ru.tsu.visapp.filters

import kotlin.math.max
import kotlin.math.min
import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import kotlinx.coroutines.coroutineScope
import ru.tsu.visapp.utils.PixelsEditor

class PopArt {
    private val inversion = Inversion() // Инверсия
    private val coloring = Coloring() // Изменение цвета

    private fun tripleColoring(
        pixels: IntArray,
        width: Int,
        height: Int,
        threshold1: Int,
        threshold2: Int
    ): IntArray {
        val tripledPixels = IntArray(pixels.size) { 0 }

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val tripledPixelsEditor = PixelsEditor(tripledPixels, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                val alpha = pixel.alpha
                var red = pixel.red
                var green = pixel.green
                var blue = pixel.blue

                if (red + green + blue in 0..<3 * min(threshold1, threshold2)) {
                    red = 0
                    green = 0
                    blue = 0
                } else if (red + green + blue in
                    3 * min(threshold1, threshold2)..<3 * max(threshold1, threshold2)
                ) {
                    red = 128
                    green = 128
                    blue = 128
                } else {
                    red = 255
                    green = 255
                    blue = 255
                }

                tripledPixelsEditor.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return tripledPixels
    }

    suspend fun popArtFiltering(
        pixels: IntArray,
        width: Int,
        height: Int,
        threshold1: Int,
        threshold2: Int
    ): IntArray = coroutineScope {
        val resultPixels = IntArray(pixels.size * 4) { 0 }

        val resultPixelsEditor = PixelsEditor(
            resultPixels,
            2 * width,
            2 * height
        )

        // Получение изображения в 3-х оттенках
        val thresholdedImage = tripleColoring(
            pixels,
            width,
            height,
            threshold1,
            threshold2
        )

        // Первое изображение
        val firstImage = coloring.coloring(
            inversion.inverse(
                thresholdedImage,
                width,
                height,
                isRedInverting = true,
                isGreenInverting = false,
                isBlueInverting = false,
                0,
                0,
                width - 1,
                height - 1
            ),
            width,
            height,
            0,
            100,
            0,
            0,
            0,
            width - 1,
            height - 1
        )

        val firstImageEditor = PixelsEditor(firstImage, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                resultPixelsEditor.setPixel(
                    i,
                    j,
                    firstImageEditor.getPixel(i, j)
                )
            }
        }

        // Второе изображение
        val secondImage = coloring.coloring(
            inversion.inverse(
                thresholdedImage,
                width,
                height,
                isRedInverting = false,
                isGreenInverting = false,
                isBlueInverting = true,
                0,
                0,
                width - 1,
                height - 1
            ),
            width,
            height,
            200,
            0,
            0,
            0,
            0,
            width - 1,
            height - 1
        )

        val secondImageEditor = PixelsEditor(secondImage, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                resultPixelsEditor.setPixel(
                    i + width,
                    j,
                    secondImageEditor.getPixel(i, j)
                )
            }
        }

        // Третье изображение
        val thirdImage = coloring.coloring(
            inversion.inverse(
                thresholdedImage,
                width,
                height,
                isRedInverting = false,
                isGreenInverting = true,
                isBlueInverting = false,
                0,
                0,
                width - 1,
                height - 1
            ),
            width,
            height,
            0,
            0,
            100,
            0,
            0,
            width - 1,
            height - 1
        )

        val thirdImageEditor = PixelsEditor(thirdImage, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                resultPixelsEditor.setPixel(
                    i,
                    j + height,
                    thirdImageEditor.getPixel(i, j)
                )
            }
        }

        // Четвёртое изображение
        val fourthImage = coloring.coloring(
            inversion.inverse(
                thresholdedImage,
                width,
                height,
                isRedInverting = true,
                isGreenInverting = false,
                isBlueInverting = true,
                0,
                0,
                width - 1,
                height - 1
            ),
            width,
            height,
            200,
            0,
            0,
            0,
            0,
            width - 1,
            height - 1
        )

        val fourthImageEditor = PixelsEditor(fourthImage, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                resultPixelsEditor.setPixel(
                    i + width,
                    j + height,
                    fourthImageEditor.getPixel(i, j)
                )
            }
        }

        return@coroutineScope resultPixels
    }
}