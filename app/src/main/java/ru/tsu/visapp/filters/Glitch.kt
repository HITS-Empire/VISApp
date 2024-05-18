package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class Glitch {
    private lateinit var pixelsEditor: PixelsEditor
    // private lateinit var pixelsEditorResult: PixelsEditor

    // private lateinit var resultPixels: IntArray

    private fun scaleDelta(delta: Int, width: Int): Int {
        return (delta.toDouble() * width.toDouble() / 1500.0).toInt()
    }

    private fun anaglyph(
        pixels: IntArray,
        width: Int,
        height: Int,
        delta: Int
    ): IntArray {
        val glitchedPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        val pixelsEditorGlitched = PixelsEditor(glitchedPixels, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                if (i - delta < 0 || i + delta >= width) {
                    pixelsEditorGlitched.setPixel(
                        i,
                        j,
                        Color.argb(
                            pixel.alpha,
                            pixel.red,
                            pixel.green,
                            pixel.blue
                        )
                    )
                    continue
                }

                val alpha = pixel.alpha
                val red = pixelsEditor.getPixel(i - delta, j)?.red ?: 0
                val green = pixelsEditor.getPixel(i + delta, j)?.green ?: 0
                val blue = pixelsEditor.getPixel(i + delta, j)?.blue ?: 0

                pixelsEditorGlitched.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return glitchedPixels
    }

    fun rgbGlitch(
        pixels: IntArray,
        width: Int,
        height: Int,
        delta: Int
    ): IntArray {
        return anaglyph(
            pixels,
            width,
            height,
            scaleDelta(delta, width)
        )
    }
}