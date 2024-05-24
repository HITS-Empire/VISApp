package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class Coloring {
    private lateinit var pixelsEditor: PixelsEditor
    private lateinit var pixelsEditorResult: PixelsEditor

    private lateinit var resultPixels: IntArray

    fun coloring(
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
    ): IntArray {
        resultPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                val alpha = pixel.alpha
                var red = pixel.red
                var green = pixel.green
                var blue = pixel.blue

                if (i in left..right &&
                    j in top..bottom
                ) {
                    red += redValue
                    green += greenValue
                    blue += blueValue

                    red = red.coerceIn(0, 255)
                    green = green.coerceIn(0, 255)
                    blue = blue.coerceIn(0, 255)
                }

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }
}