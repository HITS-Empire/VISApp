package ru.tsu.visapp

import kotlin.math.*
import android.os.Bundle
import android.graphics.Bitmap
import android.widget.ImageView
import ru.tsu.visapp.utils.ImageEditor

/*
 * Экран для 3D-куба
 */

// Двумерный вектор
class Vec2( var x: Float, var y: Float) {

    constructor(value: Float) : this(value, value)

    fun plus(newVec2: Vec2) : Vec2 {
        return Vec2(x + newVec2.x, y + newVec2.y)
    }

    fun minus(newVec2: Vec2) : Vec2 {
        return Vec2(x - newVec2.x, y - newVec2.y)
    }

    fun division(newVec2: Vec2) : Vec2 {
        val newX = if (newVec2.x == 0.0f) 0.0f else x / newVec2.x
        val newY = if (newVec2.y == 0.0f) 0.0f else y / newVec2.y

        return Vec2(newX, newY)
    }

    fun multiplication(newVec2: Vec2) : Vec2 {
        return Vec2(x * newVec2.x, y * newVec2.y)
    }

    fun length() : Float {
        return sqrt(x * x + y * y)
    }

    fun normalize() {
        division(Vec2(length()))
    }
}

// Трехмерный вектор
class Vec3(var x: Float, var y: Float, var z: Float) {

    constructor(value: Float) : this(value, value, value)

    fun plus(newVec3: Vec3) : Vec3 {
        return Vec3(x + newVec3.x, y + newVec3.y, z + newVec3.z)
    }

    fun minus(newVec3: Vec3) : Vec3 {
        return Vec3(x - newVec3.x, y - newVec3.y, z - newVec3.z)
    }

    fun division(newVec3: Vec3) : Vec3 {
        val newX = if (newVec3.x == 0.0f) 0.0f else x / newVec3.x
        val newY = if (newVec3.y == 0.0f) 0.0f else y / newVec3.y
        val newZ = if (newVec3.z == 0.0f) 0.0f else z / newVec3.z

        return Vec3(newX, newY, newZ)
    }

    fun multiplication(newVec3: Vec3) : Vec3 {
        return Vec3(x * newVec3.x, y * newVec3.y, z * newVec3.z)
    }

    fun length() : Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize() : Vec3 {
        return division(Vec3(length()))
    }

    fun changeSign() : Vec3 {
        return Vec3(-x, -y, -z)
    }

    fun module() : Vec3 {
        return Vec3(abs(x), abs(y), abs(z))
    }

    fun changeElements(newVec3: Vec3) {
        x = newVec3.x
        y = newVec3.y
        z = newVec3.z
    }

    private fun checkSignOfCoordinate(value: Float) : Float {
        return when {
            value > 0.0f -> 1.0f
            value < 0.0f -> -1.0f
            else -> 0.0f
        }
    }

    fun checkSign() : Vec3 {
        return Vec3(
            checkSignOfCoordinate(x),
            checkSignOfCoordinate(y),
            checkSignOfCoordinate(z)
        )
    }

    private fun checkEdgeUtil(edge: Float, value: Float) : Float {
        return when {
            value > edge -> 1.0f
            else -> 0.0f
        }
    }

    fun checkEdge(edge: Vec3) : Vec3 {
        return Vec3(
            checkEdgeUtil(edge.x, x),
            checkEdgeUtil(edge.y, y),
            checkEdgeUtil(edge.z, z)
        )
    }

    fun dot(newVec3: Vec3) : Float {
        return x * newVec3.x + y * newVec3.y + z * newVec3.z
    }
}

class CubeActivity: ChildActivity() {
    fun isCorrect(value: Int, minValue: Int, maxValue: Int) : Int {
        return max(minValue, min(value, maxValue))
    }

    // Функция пересечения с кубом
    fun cube(
        camera: Vec3,
        direction: Vec3,
        size: Vec3,
        normal: Vec3
    ) : Vec2 {
        val m = Vec3(1.0f).division(direction)
        val n = m.multiplication(camera)
        val k = m.module().multiplication(size)

        val t1 = n.changeSign().minus(k)
        val t2 = n.changeSign().plus(k)

        val tN = max(max(t1.x, t1.y), t1.z)
        val tF = min(min(t2.x, t2.y), t2.z)

        if (tF != 0.0f && (tN > tF || tF < 0.0f)) {
            return Vec2(-1.0f)
        }

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        normal.changeElements(direction
            .checkSign()
            .multiplication(t1.checkEdge(yzx))
            .multiplication(t1.checkEdge(zxy))
            .changeSign()
        )

        return Vec2(tN, tF)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeView(R.layout.activity_cube)
        val imageView: ImageView = findViewById(R.id.cubeImageView)

        val imageEditor = ImageEditor(contentResolver)

        val width = 300
        val height = 300
        val ratioOfScreen : Float = (width / height).toFloat()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)

        val colors = intArrayOf(
            0x000000FF.toInt(),
            0x333333FF.toInt(),
            0x808080FF.toInt(),
            0xDCDCDCFF.toInt(),
            0xFFFFFFFF.toInt()
        )

        val light = Vec3(-2.0f,0.0f,0.0f).normalize()
        for (i in 0 until width) {
            for (j in 0 until height) {
                val xy = Vec2(i.toFloat(), j.toFloat())
                    .division(Vec2(width.toFloat(), height.toFloat()))
                    .multiplication(Vec2(2.0f))
                    .minus(Vec2(1.0f))

                xy.x *= ratioOfScreen

                val camera = Vec3(-2.0f, 0.0f, 0.0f)
                val direction = Vec3(1f, xy.x, xy.y).normalize()

                val box = Vec3(0.0f, 0.0f, 0.0f)
                val intersection = cube(camera, direction, Vec3(1.0f), box)

                if (intersection.x >= 0.0f || intersection.y >= 0.0f) {
                    val point = direction
                        .multiplication(Vec3(intersection.x))
                        .plus(camera)
                        .normalize()

                    var diff = point.dot(light)

                    var indexOfColor = (diff * 5).toInt()
                    indexOfColor = isCorrect(indexOfColor, 0, 4)

                    var color = colors[indexOfColor]
                    pixels[i + j * width] = color
                }
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, pixels)
        imageView.setImageBitmap(bitmap)
    }
}