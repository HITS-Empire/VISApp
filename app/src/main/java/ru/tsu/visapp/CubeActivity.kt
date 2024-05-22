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
    private lateinit var previousCamera : Pair<Float, Float>

    private lateinit var imagePixels : Array<IntArray>

    private fun renderCube(dx: Float, dy: Float) {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val xy = Vec2(i.toFloat(), j.toFloat()) /
                    Vec2(width.toFloat(), height.toFloat()) *
                    Vec2(2.0f) - Vec2(1.0f)

                xy.x *= ratioOfScreen

                val camera = Vec3(-currentProgress / 10.0f,0.0f,0.0f)
                val direction = Vec3(1.0f, xy.x, xy.y).normalize()

                camera.rotateY(dy / 5000)
                direction.rotateY(dy / 5000)

                camera.rotateZ(dx / 5000)
                direction.rotateZ(dx / 5000)

                val color = cube(camera, direction, Vec3(1.0f))

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
        size: Vec3
    ): Int {
        val m = Vec3(1.0f) / direction
        val k = m.module() * size
        val n = m * camera

        val t1 = n.changeSign() - k
        val t2 = n.changeSign() + k

        val tN = max(max(t1.x, t1.y), t1.z)
        val tF = min(min(t2.x, t2.y), t2.z)

        if (tN > tF) return Color.BLACK

        return when {
            t1.x > t1.y && t1.x > t1.z -> {
                if (camera.x < 0) imagePixels[0][0] else imagePixels[1][0]
            }
            t1.y > t1.x && t1.y > t1.z -> {
                if (camera.y < 0) imagePixels[2][0] else imagePixels[3][0]
            }
            t1.z > t1.x && t1.z > t1.y -> {
                if (camera.z < 0) imagePixels[4][0] else imagePixels[5][0]
            }
            else -> 0
        }
    }

    private fun startRender() {
        renderCube(previousAngle.first, previousAngle.second)
    }

    private fun getPixelsFromDrawable(id: Int) : IntArray {
        val options = BitmapFactory.Options()
        options.inScaled = false

        val imageBitmap = BitmapFactory.decodeResource(resources, id, options)
        return imageEditor.getPixelsFromBitmap(imageBitmap)
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

        var startX1: Float
        var startX2: Float
        var startDistance = 0.0f

        imageView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        startX1 = event.getX(0)
                        startX2 = event.getX(1)
                        startDistance = abs(startX1 - startX2)
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
                        val currentDistance = abs(x1 - x2)

                        if (currentDistance < startDistance && currentProgress < 100) {
                            currentProgress++
                            startRender()
                        } else if (currentDistance > startDistance && currentProgress > 15) {
                            currentProgress--
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