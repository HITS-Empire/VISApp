package ru.tsu.visapp.utils.cube

import kotlin.math.sqrt

/*
 * Двумерный вектор
 */

class Vec2(var x: Float, var y: Float) {
    constructor(value1: Int, value2: Int):
        this(value1.toFloat(), value2.toFloat())
    constructor(value: Float):
        this(value, value)
    constructor(value: Int):
        this(value.toFloat(), value.toFloat())

    val length: Float
        get() = sqrt(x * x + y * y)

    operator fun plus(vec2: Vec2): Vec2 {
        return Vec2(x + vec2.x, y + vec2.y)
    }
    operator fun plus(value: Float): Vec2 {
        return Vec2(x + value, y + value)
    }

    operator fun minus(vec2: Vec2): Vec2 {
        return Vec2(x - vec2.x, y - vec2.y)
    }
    operator fun minus(value: Float): Vec2 {
        return Vec2(x - value, y - value)
    }

    operator fun times(vec2: Vec2): Vec2 {
        return Vec2(x * vec2.x, y * vec2.y)
    }
    operator fun times(value: Float): Vec2 {
        return Vec2(x * value, y * value)
    }

    operator fun div(vec2: Vec2): Vec2 {
        return Vec2(x / vec2.x, y / vec2.y)
    }
    operator fun div(value: Float): Vec2 {
        return Vec2(x / value, y / value)
    }

    fun normalize() {
        this / Vec2(length)
    }
}