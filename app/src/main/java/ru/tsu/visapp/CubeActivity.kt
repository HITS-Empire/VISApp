package ru.tsu.visapp

import kotlin.math.*
import android.os.Bundle
import android.widget.SeekBar
import android.graphics.Color
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.filtersSeekBar.*

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

class CubeActivity: ChildActivity() {
    private val width = 100
    private val height = 100
    private val ratioOfScreen : Float = (width / height).toFloat()

    private lateinit var seekBarLayout: ConstraintLayout // Контейнеры для ползунков
    private lateinit var seekBarTitle: TextView // Названия ползунков
    private lateinit var seekBar: SeekBar // Сами ползунки
    private lateinit var seekBarEditor: EditText // Отображения текущего значения
    private lateinit var seekBarUnit: TextView // Единицы измерения ползунка

    private lateinit var instruction: Instruction // Текущая инструкция

    private lateinit var bitmap : Bitmap

    private lateinit var imageView: ImageView
    private lateinit var imageEditor: ImageEditor

    private val colors = intArrayOf(
        0x000000FF.toInt(),
        0x333333FF.toInt(),
        0x808080FF.toInt(),
        0xDCDCDCFF.toInt(),
        0xFFFFFFFF.toInt()
    )

    fun renderCube(
        dx: Float,
        dy: Float,
        dz: Float,
        imageView: ImageView,
    ) {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val xy = Vec2(i.toFloat(), j.toFloat())
                    .division(Vec2(width.toFloat(), height.toFloat()))
                    .multiplication(Vec2(2.0f))
                    .minus(Vec2(1.0f))

                xy.x *= ratioOfScreen

                var camera = Vec3(-2.5f,0.0f,0.0f)
                val direction = Vec3(1f, xy.x, xy.y).normalize()

                camera.rotateY((dy / 3000))
                direction.rotateY((dy / 3000))

                camera.rotateZ((dx / 3000))
                direction.rotateZ((dx / 3000))

                val box = Vec3(0.0f, 0.0f, 0.0f)

                val result = cube(camera, direction, Vec3(1.0f), box)
                val intersection = result.first
                val color = result.second

                if (intersection.x > 0.0f) {
                    pixels[i + j * width] = color
                }
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, pixels)
        imageView.setImageBitmap(bitmap)
    }

    // Функция пересечения с кубом
    fun cube(
        camera: Vec3,
        direction: Vec3,
        size: Vec3,
        normal: Vec3
    ) : Pair<Vec2, Int> {
        val m = Vec3(1.0f).division(direction)
        val n = m.multiplication(camera)
        val k = m.module().multiplication(size)

        val t1 = n.changeSign().minus(k)
        val t2 = n.changeSign().plus(k)

        val tN = max(max(t1.x, t1.y), t1.z)
        val tF = min(min(t2.x, t2.y), t2.z)
//if (tF != 0.0f && (tN > tF || tF < 0.0f)) {

        if ((tN > tF || tF < 0.0f)) {
            return Pair(Vec2(-1.0f), Color.BLACK)
        }

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        normal.changeElements(direction
            .checkSign()
            .multiplication(t1.checkEdge(yzx))
            .multiplication(t1.checkEdge(zxy))
            .changeSign()
        )

        return Pair(Vec2(tN, tF), when {
            t1.x > t1.y && t1.x > t1.z -> Color.RED// Передняя грань
            t1.y > t1.x && t1.y > t1.z -> Color.BLUE // Правая грань
            t1.z > t1.x && t1.z > t1.y -> Color.WHITE // Верхняя грань
            t1.x < t1.y && t1.x < t1.z -> Color.GREEN // Нижняя грань
            t1.y < t1.x && t1.y < t1.z -> Color.YELLOW // Левая грань
            else -> Color.CYAN // Нижняя грань
        })
    }

    fun stop() {
        //
    }

    private val onSeekBarChangeListener = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser) return

            instruction.items[0].progress = progress
            seekBarEditor.setText(progress.toString())
            renderCube(0.0f, 0.0f, -progress.toFloat(), imageView)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) = stop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeView(R.layout.activity_cube)

        imageView = findViewById(R.id.cubeImageView)
        imageEditor = ImageEditor(contentResolver)

        seekBarLayout = findViewById(R.id.cubeSeekBarLayout)

        seekBarTitle = findViewById(R.id.cubeSeekBarTitle)

        seekBar = findViewById(R.id.cubeSeekBar)

        seekBarEditor = findViewById(R.id.cubeSeekBarEditor)

        seekBarUnit = findViewById(R.id.cubeSeekBarUnit)

        instruction = Instruction(
            R.id.cubeImageView,
            arrayOf(
                Item(),
                Item(0, 10, "Дальность камеры"),
                Item()
            )
        )

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        seekBarEditor.addTextChangedListener { editable ->
            val item = instruction.items[0]

            val text = editable.toString()
            val progress = Integer.min(
                item.max,
                Integer.max(
                    0,
                    if (text == "") 0 else text.toInt()
                )
            )
            val trim = progress.toString()

            if (text == trim) {
                item.progress = progress
                seekBar.progress = progress

                renderCube(0.0f, 0.0f, -progress.toFloat(), imageView)
            } else {
                seekBarEditor.setText(trim)
            }
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        renderCube(1.0f, 1.0f, 0f, imageView)

        var previousX = 0.0f
        var previousY = 0.0f

        var previousAngle = Pair(0.0f, 0.0f)
        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    previousX = event.x
                    previousY = event.y

                }

                MotionEvent.ACTION_MOVE -> {
                    imageEditor.clearBitmap(bitmap)

                    val dx = event.x - previousX
                    val dy = event.y - previousY

                    val angle = Pair(dx, dy)
                    renderCube(
                        angle.first + previousAngle.first,
                        angle.second + previousAngle.second,
                        0f,
                            imageView)

                    previousAngle = Pair(previousAngle.first + dx, previousAngle.second + dy)
                }
            }

            true
        }
    }
}