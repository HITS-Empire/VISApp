package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import ru.tsu.visapp.utils.PixelsEditor

class Retouching(
    private var width: Int,
    private var height: Int,
) {
    fun retouch(
        pixels: IntArray,
        x: Int,
        y: Int,
        retouchSize: Int,
        coefficient: Int
    ): IntArray {
        val result = pixels.clone()

        val pixelsEditor = PixelsEditor(pixels, width, height)
        val resultEditor = PixelsEditor(result, width, height)

        val centerX = x.coerceIn(retouchSize, width - 1 - retouchSize)
        val centerY = y.coerceIn(retouchSize, height - 1 - retouchSize)

        var totalRed = 0;
        var totalGreen = 0;
        var totalBlue = 0;
        var count = 0;

        // Считаем средний цвет
        for (i in centerX - retouchSize..centerX + retouchSize) {
            for (j in centerY - retouchSize..centerY + retouchSize) {
                val distance =
                    ((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)).toDouble()
                if (distance <= retouchSize * retouchSize) {
                    val pixel = pixelsEditor.getPixel(i, j) ?: 0

                    totalRed += pixel.red
                    totalGreen += pixel.green
                    totalBlue += pixel.blue
                    count += 1
                }
            }
        }

        totalRed /= count
        totalGreen /= count
        totalBlue /= count

        // Обновляем пиксели
        for (i in centerX - retouchSize..centerX + retouchSize) {
            for (j in centerY - retouchSize..centerY + retouchSize) {
                val distance =
                    ((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)).toDouble()
                if (distance <= retouchSize * retouchSize) {
                    val alpha = (coefficient * (1 - distance / retouchSize * retouchSize)).toInt()
                    resultEditor.setPixel(i, j, Color.argb(
                            alpha.coerceIn(0, 1),
                            totalRed.coerceIn(0, 255),
                            totalGreen.coerceIn(0, 255),
                            totalBlue.coerceIn(0, 255)
                        )
                    )
                }
            }
        }

        return result
    }
}