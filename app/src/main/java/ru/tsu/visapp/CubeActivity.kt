package ru.tsu.visapp

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

                camera.rotateY(dy / 3000)
                direction.rotateY(dy / 3000)

                camera.rotateZ(dx / 3000)
                direction.rotateZ(dx / 3000)

                val box = Vec3(0.0f, 0.0f, 0.0f)
                val color = cube(camera, direction, Vec3(1.0f), box)

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

        return when {
            t1.x > t1.y && t1.x > t1.z -> {
                if (camera.x < 0) Color.RED else Color.GREEN
            }
            t1.y > t1.x && t1.y > t1.z -> {
                if (camera.y < 0) Color.BLUE else Color.YELLOW
            }
            camera.z < 0 -> Color.WHITE
            else -> Color.CYAN
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
                    0,
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
                        angle.second + previousAngle.second
                    )

                    previousAngle = Pair(previousAngle.first + dx, previousAngle.second + dy)
                }
            }

            true
        }
    }
}