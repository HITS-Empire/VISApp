package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class ColorCorrection {
    private lateinit var pixelsEditor: PixelsEditor
    private lateinit var pixelsEditorResult: PixelsEditor

    private lateinit var resultPixels: IntArray

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

    fun correctColor(
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
    ): IntArray {
        val start = top * width + left
        val end = bottom * width + right

        println(start)
        println(end)
        println(width)
        println(height)

        resultPixels = pixels

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        // Высчитывание среднего значения серого для всего изображения
        var meanGrayScale = 0

        for (i in start ..< width) {
            for (j in end ..< height) {
                val pixel = pixelsEditor.getPixel(i, j) ?:0

                var red = pixel.red
                var green = pixel.green
                var blue = pixel.blue

                meanGrayScale += (red * 0.2126 + green * 0.7152 + blue * 0.0722).toInt()
            }
        }

        meanGrayScale /= (width * height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                val alpha = pixel.alpha
                var red = pixel.red
                var green = pixel.green
                var blue = pixel.blue

                if (j * width + i in start .. end) {
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
                }

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }
}