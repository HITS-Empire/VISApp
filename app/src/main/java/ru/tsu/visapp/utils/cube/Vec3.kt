package ru.tsu.visapp.utils.cube

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Трёхмерный вектор
 */

class Vec3(var x: Float, var y: Float, var z: Float) {
    constructor(value: Float): this(value, value, value)

    operator fun unaryMinus(): Vec3 {
        return Vec3(-x, -y, -z)
    }

    operator fun plus(vec3: Vec3): Vec3 {
        return Vec3(x + vec3.x, y + vec3.y, z + vec3.z)
    }

    operator fun minus(vec3: Vec3): Vec3 {
        return Vec3(x - vec3.x, y - vec3.y, z - vec3.z)
    }

    operator fun times(vec3: Vec3): Vec3 {
        return Vec3(x * vec3.x, y * vec3.y, z * vec3.z)
    }

    operator fun div(vec3: Vec3): Vec3 {
        return Vec3(x / vec3.x, y / vec3.y, z / vec3.z)
    }

    fun length(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vec3 {
        return this / Vec3(length())
    }

    fun module(): Vec3 {
        return Vec3(abs(x), abs(y), abs(z))
    }

    fun rotateY(angle: Float) {
        val newX = x * cos(angle) - z * sin(angle)
        val newZ = x * sin(angle) + z * cos(angle)

        x = newX
        z = newZ
    }

    fun rotateZ(angle: Float) {
        val newX = x * cos(angle) - y * sin(angle)
        val newY = x * sin(angle) + y * cos(angle)

        x = newX
        y = newY
    }
}