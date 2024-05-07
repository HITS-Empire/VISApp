package ru.tsu.visapp.utils.cube

import kotlin.math.sqrt

/*
 * Двумерный вектор
 */

class Vec2(var x: Float, var y: Float) {
    constructor(value: Float) : this(value, value)

    fun plus(newVec2: Vec2): Vec2 {
        return Vec2(x + newVec2.x, y + newVec2.y)
    }

    fun minus(newVec2: Vec2): Vec2 {
        return Vec2(x - newVec2.x, y - newVec2.y)
    }

    fun division(newVec2: Vec2): Vec2 {
        val newX = if (newVec2.x == 0.0f) 0.0f else x / newVec2.x
        val newY = if (newVec2.y == 0.0f) 0.0f else y / newVec2.y

        return Vec2(newX, newY)
    }

    fun multiplication(newVec2: Vec2): Vec2 {
        return Vec2(x * newVec2.x, y * newVec2.y)
    }

    fun length() : Float {
        return sqrt(x * x + y * y)
    }

    fun normalize() {
        division(Vec2(length()))
    }
}