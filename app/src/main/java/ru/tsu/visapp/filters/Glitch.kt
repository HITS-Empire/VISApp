package ru.tsu.visapp.filters

import kotlin.math.max
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

    private fun scaleFrequency(number: Int, width: Int): Int {
        return max(1, (number.toDouble() * width.toDouble() / 2000.0).toInt())
    }

    private fun scaleDelta(delta: Int, width: Int): Int {
        return max(1, (delta.toDouble() * width.toDouble() / 1500.0).toInt())
    }

    private fun scaleOffset(delta: Int, width: Int): Int {
        return max(1, (delta.toDouble() * width.toDouble() / 1000.0).toInt())
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

                // Формула стандартного анаглифа
                val alpha = pixel.alpha
                val red = pixelsEditor.getPixel(i - delta, j)?.red ?: 0
                val green = pixelsEditor.getPixel(i + delta, j)?.green ?: 0
                val blue = pixelsEditor.getPixel(i + delta, j)?.blue ?: 0

                pixelsEditorGlitched.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return glitchedPixels
    }

    private fun offset(
        pixels: IntArray,
        width: Int,
        height: Int,
        offsetFrequency: Int,
        offsetSize: Int
    ): IntArray {
        resultPixels = pixels

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        var offset = false // Сдвигается ли текущая строка
        var offsetProbability = offsetFrequency // Вероятность смены переменной offset

        for (j in 0..< height) {
            /* Перестаем или начинаем сдвигать с
               вероятностью offsetProbability и обновляем эту вероятность */
            if ((0..100).random() < offsetProbability) {
                offset = !offset
                offsetProbability =
                    if (offsetProbability == offsetFrequency)
                        // Чем меньше, тем шире в среднем сдвинутые прямоугольники
                        5 * width / 100
                    else
                        offsetFrequency
            }
            for (i in 0 ..< width) {
                // Случайное значение текущего сдвига в пределах offsetSize
                val currentOffset =
                    (offsetSize - offsetSize / 10 ..
                            offsetSize + offsetSize / 10).random()
                // Сдвиг
                if (offset)
                    pixelsEditorResult.setPixel(
                        i,
                        j,
                        if (i + currentOffset in 0..<width)
                            pixelsEditor.getPixel(i + currentOffset, j) ?: 0
                        else
                            pixelsEditor.getPixel(i, j) ?: 0
                    )
                else
                    pixelsEditorResult.setPixel(
                        i,
                        j,
                        pixelsEditor.getPixel(i, j) ?: 0
                    )
            }
        }

        return  resultPixels
    }

    fun rgbGlitch(
        pixels: IntArray,
        width: Int,
        height: Int,
        frequency: Int,
        delta: Int,
        offset: Int
    ): IntArray {
        return offset(
            anaglyph(
                pixels,
                width,
                height,
                scaleDelta(delta, width)
            ),
            width,
            height,
            scaleFrequency(frequency, width),
            scaleOffset(offset, width)
        )
    }
}