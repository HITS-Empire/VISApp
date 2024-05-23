package ru.tsu.visapp.utils.cube

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Трёхмерный вектор
 */

class Vec3(var x: Double, var y: Double, var z: Double) {
    constructor(value: Double) : this(value, value, value)

    fun plus(newVec3: Vec3): Vec3 {
        return Vec3(x + newVec3.x, y + newVec3.y, z + newVec3.z)
    }

    fun minus(newVec3: Vec3): Vec3 {
        return Vec3(x - newVec3.x, y - newVec3.y, z - newVec3.z)
    }

    fun division(newVec3: Vec3): Vec3 {
        val newX = if (newVec3.x == 0.0) 0.0 else x / newVec3.x
        val newY = if (newVec3.y == 0.0) 0.0 else y / newVec3.y
        val newZ = if (newVec3.z == 0.0) 0.0 else z / newVec3.z

        return Vec3(newX, newY, newZ)
    }

    fun multiplication(newVec3: Vec3): Vec3 {
        return Vec3(x * newVec3.x, y * newVec3.y, z * newVec3.z)
    }

    fun length(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vec3 {
        return division(Vec3(length()))
    }

    fun changeSign(): Vec3 {
        return Vec3(-x, -y, -z)
    }

    fun module(): Vec3 {
        return Vec3(abs(x), abs(y), abs(z))
    }

    fun changeElements(newVec3: Vec3) {
        x = newVec3.x
        y = newVec3.y
        z = newVec3.z
    }

    private fun checkSignOfCoordinate(value: Double): Double {
        return when {
            value > 0.0 -> 1.0
            value < 0.0 -> -1.0
            else -> 0.0
        }
    }

    fun checkSign(): Vec3 {
        return Vec3(
            checkSignOfCoordinate(x),
            checkSignOfCoordinate(y),
            checkSignOfCoordinate(z)
        )
    }

    private fun checkEdgeUtil(edge: Double, value: Double): Double {
        return when {
            value > edge -> 1.0
            else -> 0.0
        }
    }

    fun checkEdge(edge: Vec3): Vec3 {
        return Vec3(
            checkEdgeUtil(edge.x, x),
            checkEdgeUtil(edge.y, y),
            checkEdgeUtil(edge.z, z)
        )
    }

    fun dot(newVec3: Vec3): Double {
        return x * newVec3.x + y * newVec3.y + z * newVec3.z
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