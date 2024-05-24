package ru.tsu.visapp.filters

import kotlin.math.max
import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class Glitch {
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
        delta: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): IntArray {
        val glitchedPixels = pixels.copyOf()

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val glitchedPixelsEditor = PixelsEditor(glitchedPixels, width, height)

        for (i in 0..<width) {
            for (j in 0..<height) {
                val pixel = pixelsEditor.getPixel(i, j) ?: 0

                if (
                    i - delta < 0 ||
                    i + delta >= width ||
                    !(i in left..right && j in top..bottom)
                ) {
                    glitchedPixelsEditor.setPixel(
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

                glitchedPixelsEditor.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return glitchedPixels
    }

    private fun offset(
        pixels: IntArray,
        width: Int,
        height: Int,
        offsetFrequency: Int,
        offsetSize: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): IntArray {
        val resultPixels = pixels.copyOf()

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val resultPixelsEditor = PixelsEditor(resultPixels, width, height)

        var offset = false // Сдвигается ли текущая строка
        var offsetProbability = offsetFrequency // Вероятность смены переменной offset

        for (j in 0..<height) {
            /* Перестаём или начинаем сдвигать с вероятностью
             * offsetProbability и обновляем эту вероятность
             */
            if ((0..100).random() < offsetProbability) {
                offset = !offset

                // Чем меньше, тем шире в среднем сдвинутые прямоугольники
                offsetProbability = if (offsetProbability == offsetFrequency) {
                    5 * width / 100
                } else {
                    offsetFrequency
                }
            }
            for (i in 0..<width) {
                // Случайное значение текущего сдвига в пределах offsetSize
                val a = offsetSize - offsetSize / 10
                val b = offsetSize + offsetSize / 10
                val currentOffset = (a..b).random()

                // Сдвиг
                if (
                    offset &&
                    i in left..right &&
                    j in top..bottom
                ) {
                    resultPixelsEditor.setPixel(
                        i,
                        j,
                        if (i + currentOffset in 0..<width) {
                            pixelsEditor.getPixel(i + currentOffset, j) ?: 0
                        } else {
                            pixelsEditor.getPixel(i, j) ?: 0
                        }
                    )
                } else {
                    resultPixelsEditor.setPixel(
                        i,
                        j,
                        pixelsEditor.getPixel(i, j) ?: 0
                    )
                }
            }
        }

        return resultPixels
    }

    fun rgbGlitch(
        pixels: IntArray,
        width: Int,
        height: Int,
        frequency: Int,
        delta: Int,
        offset: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): IntArray = offset(
        anaglyph(
            pixels,
            width,
            height,
            scaleDelta(delta, width),
            left,
            top,
            right,
            bottom
        ),
        width,
        height,
        scaleFrequency(frequency, width),
        scaleOffset(offset, width),
        left,
        top,
        right,
        bottom
    )
}