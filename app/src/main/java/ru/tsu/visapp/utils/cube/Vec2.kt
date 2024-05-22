package ru.tsu.visapp.utils.cube

import kotlin.math.sqrt

/*
 * Двумерный вектор
 */

class Vec2(var x: Float, var y: Float) {
    constructor(value: Float): this(value, value)

    operator fun plus(vec2: Vec2): Vec2 {
        return Vec2(x + vec2.x, y + vec2.y)
    }

    operator fun minus(vec2: Vec2): Vec2 {
        return Vec2(x - vec2.x, y - vec2.y)
    }

    operator fun times(vec2: Vec2): Vec2 {
        return Vec2(x * vec2.x, y * vec2.y)
    }

    operator fun div(vec2: Vec2): Vec2 {
        return Vec2(x / vec2.x, y / vec2.y)
    }

    fun length() : Float {
        return sqrt(x * x + y * y)
    }

    fun normalize() {
        this / Vec2(length())
    }
}