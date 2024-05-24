package ru.tsu.visapp.utils.cube

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.min
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.alpha
import ru.tsu.visapp.filters.Scaling
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

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
        dy: Float,
        beamDirection: Vec3
    ): Int {
        val a = atan2(beamDirection.y.toDouble(), beamDirection.z.toDouble()) / PI
        val b = beamDirection.x * width
        return (a + b).toInt()
    }

    fun vec3ToUV(vec3: Vec3, width: Int, height: Int): Vec2 {
        var u: Float
        var v: Float
        val absX = abs(vec3.x)
        val absY = abs(vec3.y)
        val absZ = abs(vec3.z)
        val isXPositive = vec3.x > 0
        val isYPositive = vec3.y > 0
        val isZPositive = vec3.z > 0

        when {
            // Проекция на грань X
            absX >= absY && absX >= absZ -> {
                u = if (isXPositive) vec3.z else -vec3.z
                v = if (isXPositive) -vec3.y else vec3.y
            }
            // Проекция на грань Y
            absY >= absX && absY >= absZ -> {
                u = if (isYPositive) vec3.x else -vec3.x
                v = if (isYPositive) vec3.z else -vec3.z
            }
            // Проекция на грань Z
            else -> {
                u = if (isZPositive) vec3.x else -vec3.x
                v = if (isZPositive) -vec3.y else vec3.y
            }
        }

        // Нормализуем UV-координаты
        u = (u / max(absX, max(absY, absZ)) + 1) / 2 * width
        v = (v / max(absX, max(absY, absZ)) + 1) / 2 * height

        // Коррекция для перевернутых изображений
        if (absY >= absX && absY >= absZ) {
            val temp = u
            u = v
            v = temp
        }
//        val angle = atan2(vec3.y, vec3.x)
//
//        if (angle < 0) {
//            u = ((angle + 2 * PI) / (2 * PI) * width).toFloat()
//            v = (vec3.z + sqrt(vec3.x * vec3.x + vec3.y * vec3.y)) / (2 * sqrt(vec3.x * vec3.x + vec3.y * vec3.y)) * height
//        } else {
//            u = (angle / (2 * PI) * width).toFloat()
//            v = (vec3.z + sqrt(vec3.x * vec3.x + vec3.y * vec3.y)) / (2 * sqrt(vec3.x * vec3.x + vec3.y * vec3.y)) * height
//        }

        return Vec2(u / width, v / height)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTexture(
        pixels: IntArray,
        uv: Vec2,
        width: Int,
        height: Int
    ): Int {
        val u = (1 - uv.x.coerceIn(0.0f, 1.0f)) * (width - 1)
        val v = (uv.y.coerceIn(0.0f, 1.0f)) * (height - 1)

        val x1 = u.toInt()
        val y1 = v.toInt()
        val x2 = (x1 + 1) % width
        val y2 = (y1 + 1) % height

        val wx = u - x1
        val wy = v - y1

        val c00 = pixels[y1 * width + x1]
        val c10 = pixels[y1 * width + x2]
        val c01 = pixels[y2 * width + x1]
        val c11 = pixels[y2 * width + x2]

        val r00 = c00.red
        val g00 = c00.green
        val b00 = c00.blue
        val a00 = c00.alpha

        val r10 = c10.red
        val g10 = c10.green
        val b10 = c10.blue
        val a10 = c10.alpha

        val r01 = c01.red
        val g01 = c01.green
        val b01 = c01.blue
        val a01 = c01.alpha

        val r11 = c11.red
        val g11 = c11.green
        val b11 = c11.blue
        val a11 = c11.alpha

        val r = ((r00 * (1 - wx) + r10 * wx) * (1 - wy) + (r01 * (1 - wx) + r11 * wx) * wy).toInt()
        val g = ((g00 * (1 - wx) + g10 * wx) * (1 - wy) + (g01 * (1 - wx) + g11 * wx) * wy).toInt()
        val b = ((b00 * (1 - wx) + b10 * wx) * (1 - wy) + (b01 * (1 - wx) + b11 * wx) * wy).toInt()
        val a = ((a00 * (1 - wx) + a10 * wx) * (1 - wy) + (a01 * (1 - wx) + a11 * wx) * wy).toInt()

        return Color.argb(a, r, g, b)
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun box(
        boxSize: Vec3,
        cameraPosition: Vec3,
        beamDirection: Vec3,
        imagePixels: Array<IntArray>,
        width: Int,
        height: Int,
        i: Int,
        j: Int,
        dx: Float,
        dy: Float,
        light: Vec3,
        isTerrible: Boolean,
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
        val newColor = max(0.0f, (beamDirection - Vec3(2.0f) * Vec3(normal.dot(beamDirection)) * normal).dot(light)).pow(2)
        //val newColor = max(0.0f, normal.dot(light)) * 3 / tN
        if (!isTerrible) {
            return when {
                normal.x == -1.0f -> multiplicationColor(colors[0], 3 / tN + newColor)
                normal.y == -1.0f -> multiplicationColor(colors[1], 3 / tN + newColor)
                normal.x == 1.0f -> multiplicationColor(colors[2], 3 / tN + newColor)
                normal.y == 1.0f -> multiplicationColor(colors[3], 3 / tN + newColor)
                normal.z == 1.0f -> multiplicationColor(colors[4], 3 / tN + newColor)
                normal.z == -1.0f -> multiplicationColor(colors[5], 3 / tN + newColor)
                else -> null
            }
        }

        val index = getIndexOfImage(width, height, i, j, dx, dy, beamDirection)
        val uv = vec3ToUV(beamDirection, width, height)

        return when {
            normal.x == -1.0f -> getTexture(imagePixels[0], uv, width, height)
            normal.y == -1.0f -> getTexture(imagePixels[1], uv, width, height)
            normal.x == 1.0f -> getTexture(imagePixels[2], uv, width, height)
            normal.y == 1.0f -> getTexture(imagePixels[3], uv, width, height)
            normal.z == 1.0f -> getTexture(imagePixels[4], uv, width, height)
            normal.z == -1.0f -> getTexture(imagePixels[5], uv, width, height)
            else -> null
        }
    }
}