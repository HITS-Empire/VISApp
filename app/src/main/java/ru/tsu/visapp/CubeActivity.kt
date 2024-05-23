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
import ru.tsu.visapp.utils.PixelsEditor

/*
 * Экран для 3D-куба
 */

class CubeActivity: ChildActivity() {
    private val width = 99
    private val height = 99
    private val ratioOfScreen = (width / height).toFloat()

    private var currentProgress = 40 // Текущий прогресс в процентах

    private lateinit var bitmap: Bitmap

    private lateinit var imageView: ImageView

    private val imageEditor = ImageEditor()
    private val helper = Helper()

    private var previousAngle = Pair(0.0f, 0.0f)

    private lateinit var imagePixels: Array<IntArray>

    private fun renderCube(dx: Float, dy: Float) {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)
        val pixelsEditor = PixelsEditor(pixels, width, height)

        val cameraPosition = Vec3(-currentProgress / 10.0f, 0.0f, 0.0f)
        cameraPosition.rotateY(dy / 5000)
        cameraPosition.rotateZ(dx / 5000)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val uv = Vec2(i, j) / Vec2(width, height) * 2.0f - 1.0f

                val beamDirection = Vec3(2, uv).normalize()
                beamDirection.rotateY(dy / 5000)
                beamDirection.rotateZ(dx / 5000)

                val normal = Vec3(0)
                var color: Int? = null

                val intersection = helper.box(
                    cameraPosition,
                    beamDirection,
                    Vec3(1),
                    normal
                )
                if (intersection.x > 0.0f) {
                    color = when {
                        normal.x == -1.0f -> {
                            imagePixels[0][0]
                        }
                        normal.y == -1.0f -> {
                            imagePixels[1][0]
                        }
                        normal.x == 1.0f -> {
                            imagePixels[2][0]
                        }
                        normal.y == 1.0f -> {
                            imagePixels[3][0]
                        }
                        normal.z == -1.0f -> {
                            imagePixels[4][0]
                        }
                        normal.z == 1.0f -> {
                            imagePixels[5][0]
                        }
                        else -> null
                    }
                }

                pixelsEditor.setPixel(i, j, color)
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, pixels)
        imageView.setImageBitmap(bitmap)
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