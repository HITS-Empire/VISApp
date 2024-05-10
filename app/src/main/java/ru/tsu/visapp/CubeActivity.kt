package ru.tsu.visapp

import android.annotation.SuppressLint
import kotlin.math.*
import android.os.Bundle
import android.widget.SeekBar
import android.graphics.Color
import android.graphics.Bitmap
import android.widget.EditText
import android.view.MotionEvent
import android.widget.ImageView
import ru.tsu.visapp.utils.cube.*
import ru.tsu.visapp.utils.ImageEditor
import androidx.core.widget.addTextChangedListener

/*
 * Экран для 3D-куба
 */

class CubeActivity: ChildActivity() {
    private val width = 100
    private val height = 100
    private val ratioOfScreen = (width / height).toFloat()

    private lateinit var seekBar: SeekBar // Ползунок
    private lateinit var seekBarEditor: EditText // Отображение текущего значения
    private var currentProgress = 25 // Текущий прогресс в процентах

    private lateinit var bitmap: Bitmap

    private lateinit var imageView: ImageView

    private val imageEditor = ImageEditor()

    private var previousAngle = Pair(0.0f, 0.0f)
    private lateinit var previousCamera : Pair<Float, Float>

    private val colors = intArrayOf(
        0x000000FF.toInt(),
        0x333333FF.toInt(),
        0x808080FF.toInt(),
        0xDCDCDCFF.toInt(),
        0xFFFFFFFF.toInt()
    )

    private fun renderCube(dx: Float, dy: Float) {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val xy = Vec2(i.toFloat(), j.toFloat())
                    .division(Vec2(width.toFloat(), height.toFloat()))
                    .multiplication(Vec2(2.0f))
                    .minus(Vec2(1.0f))

                xy.x *= ratioOfScreen

                val camera = Vec3(-currentProgress / 10.0f,0.0f,0.0f)
                val direction = Vec3(1.0f, xy.x, xy.y).normalize()

                camera.rotateY(dy / 5000)
                direction.rotateY(dy / 5000)

                camera.rotateZ(dx / 5000)
                direction.rotateZ(dx / 5000)

                val box = Vec3(0.0f, 0.0f, 0.0f)
                val color = cube(camera, direction, Vec3(1.0f), box)

                previousCamera = Pair(camera.y, camera.z)

                pixels[i + j * width] = color
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, pixels)
        imageView.setImageBitmap(bitmap)
    }

    // Функция пересечения с кубом
    private fun cube(
        camera: Vec3,
        direction: Vec3,
        size: Vec3,
        normal: Vec3
    ): Int {
        val m = Vec3(1.0f).division(direction)
        val n = m.multiplication(camera)
        val k = m.module().multiplication(size)

        val t1 = n.changeSign().minus(k)
        val t2 = n.changeSign().plus(k)

        val tN = max(max(t1.x, t1.y), t1.z)
        val tF = min(min(t2.x, t2.y), t2.z)
        // if (tF != 0.0f && (tN > tF || tF < 0.0f)) {

        if (tN > tF || tF < 0.0f) {
            return Color.BLACK
        }

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        normal.changeElements(direction
            .checkSign()
            .multiplication(t1.checkEdge(yzx))
            .multiplication(t1.checkEdge(zxy))
            .changeSign()
        )

        //println("${camera.y - previousCamera.first}")
        return when {
            t1.x > t1.y && t1.x > t1.z -> if (camera.y - previousCamera.first > 0) {
                Color.RED
            } else {
                Color.GREEN
            } // Передняя грань
            t1.y > t1.x && t1.y > t1.z -> if (camera.y > 0) {
                Color.BLUE
            } else {
                Color.YELLOW
            } // Правая грань
            else -> Color.WHITE // Верхняя грань
//                t1.x < t1.y && t1.x < t1.z -> Color.GREEN // Нижняя грань
//                t1.y < t1.x && t1.y < t1.z -> Color.YELLOW // Левая грань
//                else -> Color.CYAN // Нижняя грань
        }
    }

    fun startRender() {
        renderCube(previousAngle.first, previousAngle.second)
    }

    private val onSeekBarChangeListener = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser) return

            seekBarEditor.setText(progress.toString())
            startRender()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeView(R.layout.activity_cube)

        imageView = findViewById(R.id.cubeImageView)

        seekBar = findViewById(R.id.cubeSeekBar)
        seekBarEditor = findViewById(R.id.cubeSeekBarEditor)

        seekBar.progress = currentProgress
        seekBarEditor.setText(currentProgress.toString())

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        seekBarEditor.addTextChangedListener { editable ->
            val text = editable.toString()
            val progress = Integer.min(
                100,
                Integer.max(
                    15,
                    if (text == "") 0 else text.toInt()
                )
            )
            val trim = progress.toString()

            if (text == trim) {
                currentProgress = progress
                seekBar.progress = progress

                startRender()
            } else {
                seekBarEditor.setText(trim)
            }
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        renderCube(1.0f, 1.0f)

        var previousX = 0.0f
        var previousY = 0.0f

        var startX1 = 0.0f
        var startX2 = 0.0f
        var startDistance = 0.0f
        imageView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        startX1 = event.getX(0)
                        startX2 = event.getX(1)
                        startDistance = Math.abs(startX1 - startX2)
                    }
                }

                MotionEvent.ACTION_DOWN -> {
                    previousX = event.x
                    previousY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    imageEditor.clearBitmap(bitmap)
                    if (event.pointerCount == 1) {
                        val dx = event.x - previousX
                        val dy = event.y - previousY

                        val angle = Pair(dx, dy)
                        renderCube(
                            angle.first + previousAngle.first,
                            angle.second + previousAngle.second
                        )

                        previousAngle = Pair(previousAngle.first + dx, previousAngle.second + dy)
                    } else if (event.pointerCount == 2) {
                        val x1 = event.getX(0)
                        val x2 = event.getX(1)
                        val currentDistance = Math.abs(x1 - x2)

                        if (currentDistance < startDistance && currentProgress < 100) {
                            currentProgress += 1
                            startRender()
                        } else if (currentDistance > startDistance && currentProgress > 15) {
                            currentProgress -= 1
                            startRender()
                        }
                        startDistance = currentDistance
                    }
                }
            }

            true
        }
    }
}