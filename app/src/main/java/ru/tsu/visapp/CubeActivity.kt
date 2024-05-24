package ru.tsu.visapp

import kotlin.math.*
import android.os.Bundle
import android.graphics.Color
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.ImageView
import ru.tsu.visapp.utils.cube.*
import android.graphics.BitmapFactory
import ru.tsu.visapp.utils.ImageEditor
import android.annotation.SuppressLint

/*
 * Экран для 3D-куба
 */

class CubeActivity: ChildActivity() {
    private val width = 100
    private val height = 100
    private val ratioOfScreen = (width / height).toFloat()

    private var currentProgress = 25 // Текущий прогресс в процентах

    private lateinit var bitmap: Bitmap

    private lateinit var imageView: ImageView

    private val imageEditor = ImageEditor()

    private var previousAngle = Pair(0.0f, 0.0f)

    private lateinit var imagePixels : Array<IntArray>

    private fun renderCube(dx: Float, dy: Float) {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val xy = Vec2(i.toDouble(), j.toDouble())
                    .division(Vec2(width.toDouble(), height.toDouble()))
                    .multiplication(Vec2(2.0))
                    .minus(Vec2(1.0))

                xy.x *= ratioOfScreen

                val camera = Vec3(-currentProgress / 10.0,0.0,0.0)
                val direction = Vec3(1.0, xy.x, xy.y).normalize()

                camera.rotateY(dy / 5000)
                direction.rotateY(dy / 5000)

                camera.rotateZ(dx / 5000)
                direction.rotateZ(dx / 5000)

                val box = Vec3(0.0, 0.0, 0.0)
                val color = cube(camera, direction, Vec3(1.0), box, i, j, dx, dy)

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
        normal: Vec3,
        i: Int,
        j: Int,
        dx: Float,
        dy: Float
    ): Int {
        val m = Vec3(1.0).division(direction)
        val n = m.multiplication(camera)
        val k = m.module().multiplication(size)

        var t1 = n.changeSign().minus(k)
        val t2 = n.changeSign().plus(k)

        val tN = max(max(t1.x, t1.y), t1.z)
        val tF = min(min(t2.x, t2.y), t2.z)

        if (tN > tF || tF < 0.0f) {
            return Color.BLACK
        }

        val yzx = Vec3(t1.y, t1.z, t1.x)
        val zxy = Vec3(t1.z, t1.x, t1.y)

        normal.changeElements(
            direction
                .checkSign()
                .multiplication(t1.checkEdge(yzx))
                .multiplication(t1.checkEdge(zxy))
                .changeSign()
        )

        return when {
            t1.x > t1.y && t1.x > t1.z -> {
                if (camera.x < 0) {
                    imagePixels[0][abs(i * cos(dx / 5000) - i * sin(dx / 5000)).toInt() % width + abs(j * cos(dy / 5000) - j * sin(dy / 5000)).toInt() % width * width]
                } else {
                   imagePixels[1][abs(i * cos(dx / 5000) - i * sin(dx / 5000)).toInt() % width + abs(j * cos(dy / 5000) - j * sin(dy / 5000)).toInt() % width * width]
                }
            }
            t1.y > t1.x && t1.y > t1.z -> {
                if (camera.y < 0) imagePixels[2][abs(i * cos(dx / 5000) - i * sin(dx / 5000)).toInt() % width + abs(
                    j * cos(dy / 5000) - j * sin(dy / 5000)
                ).toInt() % width * width] else imagePixels[3][abs(
                    i * cos(dx / 5000) - i * sin(
                        dx / 5000
                    )
                ).toInt() % width + abs(j * cos(dy / 5000) - j * sin(dy / 5000)).toInt() % width * width]
            }
            camera.z < 0 -> imagePixels[4][abs(i * cos(dx / 5000) - i * sin(dx / 5000)).toInt() % width + abs(
                j * cos(dy / 5000) - j * sin(dy / 5000)
            ).toInt() % width * width]
            else -> imagePixels[5][abs(i * cos(dx / 5000) - i * sin(dx / 5000)).toInt() % width + abs(
                j * cos(dy / 5000) - j * sin(dy / 5000)
            ).toInt() % width * width]
        }
    }

    fun startRender() {
        renderCube(previousAngle.first, previousAngle.second)
    }

    fun getPixelsFromDrawable(id: Int) : IntArray {
        val options = BitmapFactory.Options()
        options.inScaled = false
        val imageBitmap = BitmapFactory.decodeResource(resources, id, options)
        val currentPixels = imageEditor.getPixelsFromBitmap(imageBitmap)
        return currentPixels
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeView(R.layout.activity_cube)

        imageView = findViewById(R.id.cubeImageView)

        imagePixels = arrayOf(
            getPixelsFromDrawable(R.drawable.digit_1),
            getPixelsFromDrawable(R.drawable.digit_2),
            getPixelsFromDrawable(R.drawable.digit_3),
            getPixelsFromDrawable(R.drawable.digit_4),
            getPixelsFromDrawable(R.drawable.digit_5),
            getPixelsFromDrawable(R.drawable.digit_6)
        )

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