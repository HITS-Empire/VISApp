package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class Glitch {
    private lateinit var pixelsEditor: PixelsEditor
    private lateinit var pixelsEditorResult: PixelsEditor

    private lateinit var resultPixels: IntArray

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

    private fun getRandomRectangles(
        width: Int,
        height: Int,
        delta: Int
    ): ArrayList<ArrayList<Int>> {
        val numberOfRectangles = (1..5).random()

        val result = ArrayList<ArrayList<Int>>()

        for (i in 0 ..< numberOfRectangles) {
            val leftX = (delta until  width - delta).random()
            val leftY = (delta until  height - delta).random()

            val rightX =
                (leftX + delta until  width).random().coerceIn(0, width)
            val rightY =
                (leftY - delta until leftY - delta / 4).random().coerceIn(0, height)

            val rectangleCoordinates = arrayListOf(
                leftX,
                leftY,
                rightX,
                rightY
            )

            result.add(rectangleCoordinates)
        }

        return result
    }

    private fun offsetRectangles(
        pixels: IntArray,
        width: Int,
        height: Int,
        delta: Int,
        rectangles: ArrayList<ArrayList<Int>>
    ): IntArray {
        resultPixels = pixels

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        for (rectangle in rectangles) {
            for (i in rectangle[0] ..< rectangle[2]) {
                for (j in rectangle[1]..< rectangle[3]) {
                    pixelsEditorResult.setPixel(
                        i - delta,
                        j,
                        pixelsEditor.getPixel(i, j)
                    )
                }
            }
        }

        return  resultPixels
    }

    fun rgbGlitch(
        pixels: IntArray,
        width: Int,
        height: Int,
        delta: Int
    ): IntArray {
        return offsetRectangles(
            anaglyph(
                pixels,
                width,
                height,
                scaleDelta(delta, width)
            ),
            width,
            height,
            scaleDelta(delta, width),
            getRandomRectangles(
                width,
                height,
                scaleDelta(delta, width)
            )
        )
    }
}