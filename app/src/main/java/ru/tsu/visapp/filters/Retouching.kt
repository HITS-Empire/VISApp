package ru.tsu.visapp.filters

import kotlin.math.pow
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor

class Retouching {
    fun retouch(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        retouchSize: Int,
        coefficient: Int
    ) {
        val pixelsEditor = PixelsEditor(pixels, width, height)

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
                    val alpha = (
                         255 * (1 - distance * (coefficient / 10) /
                                    retouchSize.toDouble().pow(2))
                    ).toInt()

                    pixelsEditor.setPixel(
                        i,
                        j,
                        Color.argb(
                            alpha.coerceIn(0, 255),
                            totalRed.coerceIn(0, 255),
                            totalGreen.coerceIn(0, 255),
                            totalBlue.coerceIn(0, 255)
                        )
                    )
                }
            }
        }
    }
}
