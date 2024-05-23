package ru.tsu.visapp.utils.cube

import kotlin.math.max
import kotlin.math.min
import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.alpha

class Helper {
    private fun Boolean.toFloat(): Float = if (this) 1.0f else 0.0f

    private fun sign(value: Float): Float {
        return (0.0f < value).toFloat() - (value < 0.0f).toFloat()
    }
    private fun sign(vec3: Vec3): Vec3 {
        return Vec3(sign(vec3.x), sign(vec3.y), sign(vec3.z))
    }

    private fun step(edge: Float, value: Float): Float {
        return (value > edge).toFloat()
    }
    private fun step(edge: Vec3, vec3: Vec3): Vec3 {
        return Vec3(step(edge.x, vec3.x), step(edge.y, vec3.y), step(edge.z, vec3.z))
    }

    private fun min(vec3: Vec3): Float {
        return min(min(vec3.x, vec3.y), vec3.z)
    }
    private fun max(vec3: Vec3): Float {
        return max(max(vec3.x, vec3.y), vec3.z)
    }

    private fun dot(color: Int, value: Float): Int {
        return Color.argb(
            color.alpha,
            (color.red * value).toInt().coerceIn(0, 255),
            (color.green * value).toInt().coerceIn(0, 255),
            (color.blue * value).toInt().coerceIn(0, 255)
        )
    }

    // Пересечение с кубом
    fun box(
        cameraPosition: Vec3,
        beamDirection: Vec3,
        boxSize: Vec3,
        imagePixels: Array<IntArray>,
        width: Int,
        height: Int,
        isTerrible: Boolean
    ): Int? {
        val m = Vec3(1) / beamDirection
        val n = m * cameraPosition
        val k = m.module() * boxSize

        val t1 = -n - k
        val t2 = -n + k

        val tN = max(t1)
        val tF = min(t2)

        if (tN > tF || tF < 0.0f || tN <= 0.0f) return null

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        val normal = -sign(beamDirection) * step(yzx, t1) * step(zxy, t1)

        return when {
            normal.x == -1.0f -> {
                if (isTerrible) {
                    imagePixels[0][0] // Код для картинок
                } else {
                    dot(imagePixels[0][0], 3 / tN)
                }
            }
            normal.y == -1.0f -> {
                if (isTerrible) {
                    imagePixels[1][0] // Код для картинок
                } else {
                    dot(imagePixels[1][0], 3 / tN)
                }
            }
            normal.x == 1.0f -> {
                if (isTerrible) {
                    imagePixels[2][0] // Код для картинок
                } else {
                    dot(imagePixels[2][0], 3 / tN)
                }
            }
            normal.y == 1.0f -> {
                if (isTerrible) {
                    imagePixels[3][0] // Код для картинок
                } else {
                    dot(imagePixels[3][0], 3 / tN)
                }
            }
            normal.z == -1.0f -> {
                if (isTerrible) {
                    imagePixels[4][0] // Код для картинок
                } else {
                    dot(imagePixels[4][0], 3 / tN)
                }
            }
            normal.z == 1.0f -> {
                if (isTerrible) {
                    imagePixels[5][0] // Код для картинок
                } else {
                    dot(imagePixels[5][0], 3 / tN)
                }
            }
            else -> null
        }
    }
}