package ru.tsu.visapp.filters

import android.graphics.Color
import kotlinx.coroutines.async
import androidx.core.graphics.red
import androidx.core.graphics.blue
import kotlinx.coroutines.awaitAll
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import ru.tsu.visapp.utils.PixelsEditor

class ColorCorrection {
    data class ProcessPixel(
        val width: Int,
        val height: Int,
        val brightnessValue: Int,
        val saturationValue: Int,
        val contrastValue: Int,
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val pixelsEditor: PixelsEditor,
        val resultPixelsEditor: PixelsEditor,
        val meanGrayScale: Int
    ) {
        private fun brightness(
            red: Int,
            green: Int,
            blue: Int,
            value: Int
        ): Array<Int> {
            return arrayOf(red + value, green + value, blue + value)
        }

        private fun saturation(
            red: Int,
            green: Int,
            blue: Int,
            value: Int
        ): Array<Int> {
            val max = if (value < 0) 255 else 128

            val grayScale = (red * 0.2126 + green * 0.7152 + blue * 0.0722).toInt()

            return arrayOf(
                red + (red - grayScale) * value / max,
                green + (green - grayScale) * value / max,
                blue + (blue - grayScale) * value / max
            )
        }

        private fun contrast(
            red: Int,
            green: Int,
            blue: Int,
            grayScale: Int,
            value: Int
        ): Array<Int> {
            val max = if (value < 0) 255 else 128

            return arrayOf(
                red + (red - grayScale) * value / max,
                green + (green - grayScale) * value / max,
                blue + (blue - grayScale) * value / max
            )
        }

        fun start(i: Int, j: Int) {
            if (i in left..right && j in top..bottom) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                val alpha = pixel.alpha
                var red = pixel.red
                var green = pixel.green
                var blue = pixel.blue

                // Яркость
                val (
                    brightRed,
                    brightGreen,
                    brightBlue
                ) = brightness(red, green, blue, brightnessValue)

                // Насыщенность
                val (
                    saturationRed,
                    saturationGreen,
                    saturationBlue
                ) = saturation(brightRed, brightGreen, brightBlue, saturationValue)

                // Контраст
                val (
                    contrastRed,
                    contrastGreen,
                    contrastBlue
                ) = contrast(
                    saturationRed,
                    saturationGreen,
                    saturationBlue,
                    meanGrayScale,
                    contrastValue
                )

                red = contrastRed.coerceIn(0, 255)
                green = contrastGreen.coerceIn(0, 255)
                blue = contrastBlue.coerceIn(0, 255)

                resultPixelsEditor.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }
    }

    suspend fun correctColor(
        pixels: IntArray,
        width: Int,
        height: Int,
        brightnessValue: Int,
        saturationValue: Int,
        contrastValue: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): IntArray = coroutineScope {
        val resultPixels = pixels.copyOf()

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val resultPixelsEditor = PixelsEditor(resultPixels, width, height)

        val halfWidth = width / 2
        val halfHeight = height / 2

        // Высчитывание среднего значения серого для всего изображения
        val meanGrayScales = arrayOf(0, 0, 0, 0)

        val firstJobs = arrayOf(
            arrayOf(0, halfWidth, 0, halfHeight, 0),
            arrayOf(halfWidth, width, 0, halfHeight, 1),
            arrayOf(0, halfWidth, halfHeight, height, 2),
            arrayOf(halfWidth, width, halfHeight, height, 3)
        ).map { a ->
            async(Dispatchers.Default) {
                for (i in a[0] until a[1]) {
                    for (j in a[2] until a[3]) {
                        if (i in left..right && j in top..bottom) {
                            val pixel = pixelsEditor.getPixel(i, j) ?: 0

                            val red = pixel.red
                            val green = pixel.green
                            val blue = pixel.blue

                            meanGrayScales[a[4]] +=
                                (red * 0.2126 + green * 0.7152 + blue * 0.0722).toInt()
                        }
                    }
                }
            }
        }

        firstJobs.awaitAll()

        var meanGrayScale = 0
        for (i in 0..3) {
            meanGrayScale += meanGrayScales[i]
        }
        meanGrayScale /= (width * height)

        val processPixel = ProcessPixel(
            width,
            height,
            brightnessValue,
            saturationValue,
            contrastValue,
            left,
            top,
            right,
            bottom,
            pixelsEditor,
            resultPixelsEditor,
            meanGrayScale
        )

        val secondJobs = arrayOf(
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

        secondJobs.awaitAll()

        return@coroutineScope resultPixels
    }
}