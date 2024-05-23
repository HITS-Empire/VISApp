package ru.tsu.visapp.utils.cube

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    // Пересечение с кубом
    fun box(
        cameraPosition: Vec3,
        beamDirection: Vec3,
        boxSize: Vec3,
        imagePixels: Array<IntArray>,
        width: Int,
        height: Int
    ): Int? {
        val m = Vec3(1) / beamDirection
        val n = m * cameraPosition
        val k = m.module() * boxSize

        val t1 = -n - k
        val t2 = -n + k

        val tN = max(max(t1.x, t1.y), t1.z)
        val tF = min(min(t2.x, t2.y), t2.z)

        if (tN > tF || tF < 0.0f || tN <= 0.0f) return null

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        val normal = -sign(beamDirection) * step(yzx, t1) * step(zxy, t1)

        return when {
            normal.x == -1.0f -> {
                imagePixels[0][0]
            }
            normal.y == -1.0f -> {
                imagePixels[1][0]
            }
            normal.x == 1.0f -> {
                imagePixels[2][0]
            }
            normal.y == 1.0f -> {
                imagePixels[3][0]
            }
            normal.z == -1.0f -> {
                imagePixels[4][0]
            }
            normal.z == 1.0f -> {
                imagePixels[5][0]
            }
            else -> null
        }
    }
}