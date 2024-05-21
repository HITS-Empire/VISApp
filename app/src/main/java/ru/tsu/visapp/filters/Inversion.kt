package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class Inversion {
    private lateinit var pixelsEditor: PixelsEditor
    private lateinit var pixelsEditorResult: PixelsEditor

    private lateinit var resultPixels: IntArray

    fun inverse(
        pixels: IntArray,
        width: Int,
        height: Int,
        isRedInverting: Boolean,
        isGreenInverting: Boolean,
        isBlueInverting: Boolean
    ): IntArray {
        resultPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                val alpha = pixel.alpha
                var red = pixel.red
                var green = pixel.green
                var blue = pixel.blue

                if (isRedInverting) red = (255 - red).coerceIn(0, 255)
                if (isGreenInverting) green = (255 - green).coerceIn(0, 255)
                if (isBlueInverting) blue = (255 - blue).coerceIn(0, 255)

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }
}