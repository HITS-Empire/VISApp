package ru.tsu.visapp.utils.cube

import kotlin.math.pow
import kotlin.math.abs
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
            absX >= absY && absX >= absZ -> {
                u = if (isXPositive) -vec3.y else vec3.y
                v = if (isXPositive) -vec3.z else -vec3.z
            }

            absY >= absX && absY >= absZ -> {
                u = if (isYPositive) -vec3.x else -vec3.x
                v = if (isYPositive) -vec3.z else -vec3.z
            }

            else -> {
                u = if (isZPositive) -vec3.y else -vec3.y
                v = if (isZPositive) vec3.x else -vec3.x
            }
        }

        u = (u / max(absX, max(absY, absZ)) + 1) / 2 * width
        v = (v / max(absX, max(absY, absZ)) + 1) / 2 * height

        return Vec2(u / width, v / height)
    }


    fun getTexture(
        pixels: IntArray,
        uv: Vec2,
        width: Int,
        height: Int
    ): Int {
        val u = (1 - uv.x.coerceIn(0.0f, 1.0f)) * (width - 1)
        val v = (1 - uv.y.coerceIn(0.0f, 1.0f)) * (height - 1)

        val xFirst = u.toInt()
        val yFirst = v.toInt()
        val xSecond = (xFirst + 1) % width
        val ySecond = (yFirst + 1) % height

        val dx = u - xFirst
        val dy = v - yFirst

        val firstColor = pixels[yFirst * width + xFirst]
        val secondColor = pixels[yFirst * width + xSecond]
        val thirdColor = pixels[ySecond * width + xFirst]
        val fourthColor = pixels[ySecond * width + xSecond]

        val rFirst = firstColor.red
        val gFirst = firstColor.green
        val bFirst = firstColor.blue
        val aFirst = firstColor.alpha

        val rThird = secondColor.red
        val gThird = secondColor.green
        val bThird = secondColor.blue
        val aThird = secondColor.alpha

        val rSecond = thirdColor.red
        val gSecond = thirdColor.green
        val bSecond = thirdColor.blue
        val aSecond = thirdColor.alpha

        val rFourth = fourthColor.red
        val gFourth = fourthColor.green
        val bFourth = fourthColor.blue
        val aFourth = fourthColor.alpha

        val r = ((rFirst * (1 - dx) + rThird * dx) * (1 - dy) + (rSecond * (1 - dx) + rFourth * dx) * dy).toInt()
        val g = ((gFirst * (1 - dx) + gThird * dx) * (1 - dy) + (gSecond * (1 - dx) + gFourth * dx) * dy).toInt()
        val b = ((bFirst * (1 - dx) + bThird * dx) * (1 - dy) + (bSecond * (1 - dx) + bFourth * dx) * dy).toInt()
        val a = ((aFirst * (1 - dx) + aThird * dx) * (1 - dy) + (aSecond * (1 - dx) + aFourth * dx) * dy).toInt()

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
    fun box(
        boxSize: Vec3,
        cameraPosition: Vec3,
        beamDirection: Vec3,
        imagePixels: Array<Pair<IntArray, Int>>,
        width: Int,
        height: Int,
        light: Vec3,
        isTerrible: Boolean,
    ): Int? {
        val perpendicular = Vec3(1) / beamDirection
        val direction = perpendicular * cameraPosition
        val cube = perpendicular.module() * boxSize

        val t1 = -direction - cube
        val t2 = -direction + cube

        val tN = max(t1)
        val tF = min(t2)

        if (tN > tF || tF < 0.0f || tN <= 0.0f) return null

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        val normal = -sign(beamDirection) * step(yzx, t1) * step(zxy, t1)
        val newColor = max(
            0.0f,
            (beamDirection - Vec3(2.0f) * Vec3(normal.dot(beamDirection)) * normal).dot(light)
        ).pow(2)



        if (!isTerrible) {
            return when {
                normal.x == -1.0f -> multiplicationColor(colors[0], 2 / tN + newColor / 10)
                normal.y == -1.0f -> multiplicationColor(colors[1], 2 / tN + newColor / 10)
                normal.x == 1.0f -> multiplicationColor(colors[2], 2 / tN + newColor / 10)
                normal.y == 1.0f -> multiplicationColor(colors[3], 2 / tN + newColor / 10)
                normal.z == 1.0f -> multiplicationColor(colors[4], 2 / tN + newColor / 10)
                normal.z == -1.0f -> multiplicationColor(colors[5], 2 / tN + newColor / 10)
                else -> null
            }
        }

        val uv = vec3ToUV(beamDirection, width, height)

        return when {
            normal.x == -1.0f -> getTexture(
                imagePixels[0].first,
                uv,
                imagePixels[0].second,
                imagePixels[0].first.size / imagePixels[0].second
            )

            normal.y == -1.0f -> getTexture(
                imagePixels[1].first,
                uv,
                imagePixels[1].second,
                imagePixels[1].first.size / imagePixels[1].second
            )

            normal.x == 1.0f -> getTexture(
                imagePixels[2].first,
                uv,
                imagePixels[2].second,
                imagePixels[2].first.size / imagePixels[2].second
            )

            normal.y == 1.0f -> getTexture(
                imagePixels[3].first,
                uv,
                imagePixels[3].second,
                imagePixels[3].first.size / imagePixels[3].second
            )

            normal.z == 1.0f -> getTexture(
                imagePixels[4].first,
                uv,
                imagePixels[4].second,
                imagePixels[4].first.size / imagePixels[4].second
            )

            normal.z == -1.0f -> getTexture(
                imagePixels[5].first,
                uv,
                imagePixels[5].second,
                imagePixels[5].first.size / imagePixels[5].second
            )

            else -> null
        }
    }
}