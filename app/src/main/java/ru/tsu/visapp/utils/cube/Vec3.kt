package ru.tsu.visapp.utils.cube

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Трёхмерный вектор
 */

class Vec3(var x: Float, var y: Float, var z: Float) {
    constructor(value1: Int, value2: Int, value3: Int):
        this(value1.toFloat(), value2.toFloat(), value3.toFloat())
    constructor(value: Float):
        this(value, value, value)
    constructor(value: Float, vec2: Vec2):
            this(value, vec2.x, vec2.y)
    constructor(value: Int):
        this(value.toFloat(), value.toFloat(), value.toFloat())
    constructor(value: Int, vec2: Vec2):
        this(value.toFloat(), vec2.x, vec2.y)

    val length: Float
        get() = sqrt(x * x + y * y + z * z)

    operator fun unaryMinus(): Vec3 {
        return Vec3(-x, -y, -z)
    }

    operator fun plus(vec3: Vec3): Vec3 {
        return Vec3(x + vec3.x, y + vec3.y, z + vec3.z)
    }
    operator fun plus(value: Float): Vec3 {
        return Vec3(x + value, y + value, z + value)
    }

    operator fun minus(vec3: Vec3): Vec3 {
        return Vec3(x - vec3.x, y - vec3.y, z - vec3.z)
    }
    operator fun minus(value: Float): Vec3 {
        return Vec3(x - value, y - value, z - value)
    }

    operator fun times(vec3: Vec3): Vec3 {
        return Vec3(x * vec3.x, y * vec3.y, z * vec3.z)
    }
    operator fun times(value: Float): Vec3 {
        return Vec3(x * value, y * value, z * value)
    }

    operator fun div(vec3: Vec3): Vec3 {
        return Vec3(x / vec3.x, y / vec3.y, z / vec3.z)
    }
    operator fun div(value: Float): Vec3 {
        return Vec3(x / value, y / value, z / value)
    }

    fun normalize(): Vec3 {
        return this / Vec3(length)
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

    fun set(vec3: Vec3) {
        x = vec3.x
        y = vec3.y
        z = vec3.z
    }
}