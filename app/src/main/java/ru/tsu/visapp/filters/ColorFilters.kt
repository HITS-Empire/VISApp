package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class ColorFilters {
    private lateinit var pixelsEditor: PixelsEditor
    private lateinit var pixelsEditorResult: PixelsEditor

    fun saturation(
        pixels: IntArray,
        width: Int,
        height: Int,
        value: Int
    ): IntArray {
        val resultPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        for (i in 0 ..< width) {
            for (j in 0 ..< height) {
                val pixel = pixelsEditor.getPixel(i, j)

                val alpha = pixel?.alpha ?: 0
                var red = pixel?.red ?: 0
                var green = pixel?.green ?: 0
                var blue = pixel?.blue ?: 0

                val max = if (value < 0) 255 else 128

                val grayScale = (red * 0.2126 + green * 0.7152 + blue * 0.0722).toInt()

                red += (red - grayScale) * value / max;
                green += (green - grayScale) * value / max;
                blue += (blue - grayScale) * value / max;

                red = red.coerceIn(0, 255)
                green = green.coerceIn(0, 255)
                blue = blue.coerceIn(0, 255)

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }

    fun contrast(
        pixels: IntArray,
        width: Int,
        height: Int,
        value: Int
    ): IntArray {
        val resultPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        var grayScale = 0

        // Высчитывание среднего значения серого для всего изображения
        for (i in 0 ..< width) {
            for (j in 0 ..< height) {
                val pixel = pixelsEditor.getPixel(i, j)

                var red = pixel?.red ?: 0
                var green = pixel?.green ?: 0
                var blue = pixel?.blue ?: 0

                grayScale += (
                        red * 0.2126 +
                        green * 0.7152 +
                        blue * 0.0722
                ).toInt()
            }
        }

        grayScale /= (width * height)

        for (i in 0 ..< width) {
            for (j in 0 ..< height) {
                val pixel = pixelsEditor.getPixel(i, j)

                val alpha = pixel?.alpha ?: 0
                var red = pixel?.red ?: 0
                var green = pixel?.green ?: 0
                var blue = pixel?.blue ?: 0

                val max = if (value < 0) 255 else 128

                red += (red - grayScale) * value / max;
                green += (green - grayScale) * value / max;
                blue += (blue - grayScale) * value / max;

                red = red.coerceIn(0, 255)
                green = green.coerceIn(0, 255)
                blue = blue.coerceIn(0, 255)

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }

    fun brightness(
        pixels: IntArray,
        width: Int,
        height: Int,
        value: Int
    ): IntArray {
        val resultPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        for (i in 0 ..< width) {
            for (j in 0 ..< height) {
                val pixel = pixelsEditor.getPixel(i, j)

                val alpha = pixel?.alpha ?: 0
                var red = pixel?.red ?: 0
                var green = pixel?.green ?: 0
                var blue = pixel?.blue ?: 0

                red += value;
                green += value;
                blue += value;

                red = red.coerceIn(0, 255)
                green = green.coerceIn(0, 255)
                blue = blue.coerceIn(0, 255)

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }
}