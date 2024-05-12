package ru.tsu.visapp.filters

import kotlin.math.pow
import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.alpha
import ru.tsu.visapp.utils.PixelsEditor
import kotlin.math.min

class Retouching {
    private fun retouchScale(
        retouchSize: Int,
        width: Int,
        height: Int
    ): Int {
        return ((retouchSize.toDouble() / 101) * min(width, height) / 2).toInt()
    }

    fun retouch(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        initRetouchSize: Int,
        coefficient: Int
    ) {
        val pixelsEditor = PixelsEditor(pixels, width, height)

        val retouchSize = retouchScale(initRetouchSize, width, height)

        val centerX = x.coerceIn(retouchSize, width - 1 - retouchSize)
        val centerY = y.coerceIn(retouchSize, height - 1 - retouchSize)

        var totalRed = 0
        var totalGreen = 0
        var totalBlue = 0
        var count = 0

        // Считаем средний цвет
        for (i in centerX - retouchSize .. centerX + retouchSize) {
            for (j in centerY - retouchSize .. centerY + retouchSize) {
                val a = (i - centerX).toDouble().pow(2)
                val b = (j - centerY).toDouble().pow(2)
                if (a + b <= retouchSize.toDouble().pow(2)) {
                    val pixel = pixelsEditor.getPixel(i, j) ?: 0

                    totalRed += pixel.red
                    totalGreen += pixel.green
                    totalBlue += pixel.blue
                    count++
                }
            }
        }

        totalRed /= count
        totalGreen /= count
        totalBlue /= count

        // Обновляем пиксели
        for (i in centerX - retouchSize .. centerX + retouchSize) {
            for (j in centerY - retouchSize .. centerY + retouchSize) {
                val a = (i - centerX).toDouble().pow(2)
                val b = (j - centerY).toDouble().pow(2)
                val distance = a + b

                if (distance <= retouchSize.toDouble().pow(2)) {
                    val color = pixelsEditor.getPixel(i, j) ?: 0
                    val alpha = (
                        1 - distance / retouchSize.toDouble().pow(2)
                    ) * ((coefficient + 5).toDouble() / 16)

                    val red = color.red * (1 - alpha) + totalRed * alpha
                    val green = color.green * (1 - alpha) + totalGreen * alpha
                    val blue = color.blue * (1 - alpha) + totalBlue * alpha

                    pixelsEditor.setPixel(
                        i,
                        j,
                        Color.argb(
                            color.alpha,
                            red.toInt(),
                            green.toInt(),
                            blue.toInt()
                        )
                    )
                }
            }
        }
    }
}
