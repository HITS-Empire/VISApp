package ru.tsu.visapp.filters

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

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
        var result = pixels.clone()

        val centerX = x.coerceIn(retouchSize, width - 1 - retouchSize)
        val centerY = y.coerceIn(retouchSize, height - 1 - retouchSize)

        for (i in centerX - retouchSize..centerX + retouchSize) {
            for (j in centerY - retouchSize..centerY + retouchSize) {
                val distance =
                    ((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)).toDouble()
                if (distance <= retouchSize * retouchSize) {
                    val pixel = pixels[j * width + i]

                    val resultPixel = Color.argb(
                        pixel.alpha,
                        (pixel.red * coefficient).coerceIn(0, 255),
                        (pixel.green * coefficient).coerceIn(0, 255),
                        (pixel.blue * coefficient).coerceIn(0, 255)
                    )

                    result[j * width + i] = resultPixel
                }
            }
        }

        return result
    }
}