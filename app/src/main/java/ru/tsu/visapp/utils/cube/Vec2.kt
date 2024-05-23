package ru.tsu.visapp.utils.cube

import kotlin.math.sqrt

/*
 * Двумерный вектор
 */

class Vec2(var x: Double, var y: Double) {
    constructor(value: Double) : this(value, value)

    fun plus(newVec2: Vec2): Vec2 {
        return Vec2(x + newVec2.x, y + newVec2.y)
    }

    fun minus(newVec2: Vec2): Vec2 {
        return Vec2(x - newVec2.x, y - newVec2.y)
    }

    fun division(newVec2: Vec2): Vec2 {
        val newX = if (newVec2.x == 0.0) 0.0 else x / newVec2.x
        val newY = if (newVec2.y == 0.0) 0.0 else y / newVec2.y

        return Vec2(newX, newY)
    }

    fun multiplication(newVec2: Vec2): Vec2 {
        return Vec2(x * newVec2.x, y * newVec2.y)
    }

    fun length() : Double {
        return sqrt(x * x + y * y)
    }

    fun normalize() {
        division(Vec2(length()))
    }
}