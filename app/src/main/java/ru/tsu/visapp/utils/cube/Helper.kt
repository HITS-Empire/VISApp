package ru.tsu.visapp.utils.cube

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.min
import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.alpha

class Helper {
    // Цвета сторон куба по умолчанию
    private val colors = arrayOf(
        Color.argb(255, 43, 203, 17),
        Color.argb(255, 255, 90, 37),
        Color.argb(255, 255, 207, 37),
        Color.argb(255, 255, 37, 103),
        Color.argb(255, 144, 37, 252),
        Color.argb(255, 37, 175, 252)
    )

    // Получить нужный индекс пикселя
    private fun getIndexOfImage(
        width: Int,
        height: Int,
        i: Int,
        j: Int,
        dx: Float,
        dy: Float
    ): Int {
        return (
            abs(i * cos(dx) - i * sin(dx)).toInt() % width
        ) + (
            abs(j * cos(dy) - j * sin(dy)).toInt() % width * height
        )
    }

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

    private fun multiplicationColor(color: Int, value: Float): Int {
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
        i: Int,
        j: Int,
        dx: Float,
        dy: Float,
        light: Vec3,
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

        val newColor = 3.0f / tN // normal.dot(light) * 0.5f + 0.5f
        if (!isTerrible) {
            return when {
                normal.x == -1.0f -> multiplicationColor(colors[0], newColor)
                normal.y == -1.0f -> multiplicationColor(colors[1], newColor)
                normal.x == 1.0f -> multiplicationColor(colors[2], newColor)
                normal.y == 1.0f -> multiplicationColor(colors[3], newColor)
                normal.z == 1.0f -> multiplicationColor(colors[4], newColor)
                normal.z == -1.0f -> multiplicationColor(colors[5], newColor)
                else -> null
            }
        }

        val index = getIndexOfImage(width, height, i, j, dx, dy)

        return when {
            normal.x == -1.0f -> multiplicationColor(imagePixels[0][index], newColor)
            normal.y == -1.0f -> multiplicationColor(imagePixels[1][index], newColor)
            normal.x == 1.0f -> multiplicationColor(imagePixels[2][index], newColor)
            normal.y == 1.0f -> multiplicationColor(imagePixels[3][index], newColor)
            normal.z == 1.0f -> multiplicationColor(imagePixels[4][index], newColor)
            normal.z == -1.0f -> multiplicationColor(imagePixels[5][index], newColor)
            else -> null
        }
    }
}