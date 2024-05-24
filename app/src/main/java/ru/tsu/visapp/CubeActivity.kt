package ru.tsu.visapp

import kotlin.math.*
import android.os.Bundle
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.ImageView
import ru.tsu.visapp.utils.cube.*
import android.graphics.BitmapFactory
import ru.tsu.visapp.utils.ImageEditor
import android.annotation.SuppressLint
import android.widget.Button
import ru.tsu.visapp.utils.PixelsEditor

/*
 * Экран для 3D-куба
 */

class CubeActivity: ChildActivity() {
    private val width = 100 // Ширина картинки
    private val height = 100 // Высота картинки

    private var currentProgress = 50 // Текущий прогресс в процентах
    private var isTerrible = false // Включен ли режим позорного куба

    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var imagePixels: Array<IntArray>
    private lateinit var modeButton: Button

    private val imageEditor = ImageEditor()
    private val helper = Helper()

    private var dx = 0.4f // Угол по X
    private var dy = 0.4f // Угол по Y

    private fun renderCube() {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)
        val pixelsEditor = PixelsEditor(pixels, width, height)

        val cameraPosition = Vec3(-currentProgress / 10.0f, 0.0f, 0.0f)
        cameraPosition.rotateY(dy)
        cameraPosition.rotateZ(dx)

        val light = Vec3(-0.5f,dx,dy).normalize()

        for (i in 0 until width) {
            for (j in 0 until height) {
                val uv = Vec2(i, j) / Vec2(width, height) * 2.0f - 1.0f

                val beamDirection = Vec3(2, uv).normalize()
                beamDirection.rotateY(dy)
                beamDirection.rotateZ(dx)

                val color = helper.box(
                    cameraPosition,
                    beamDirection,
                    Vec3(1),
                    imagePixels,
                    width,
                    height,
                    isTerrible,
                    i,
                    j,
                    dy,
                    dx,
                    light,
                )
                pixelsEditor.setPixel(i, j, color ?: 0)
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, pixels)
        imageView.setImageBitmap(bitmap)
    }

    private fun getPixelsFromDrawable(id: Int): IntArray {
        val options = BitmapFactory.Options()
        options.inScaled = false

        val imageBitmap = BitmapFactory.decodeResource(resources, id, options)
        return imageEditor.getPixelsFromBitmap(imageBitmap)
    }

    private fun changeMode(mode: Boolean) {
        isTerrible = mode

        modeButton.text = if (isTerrible) {
            "Перейти в красивый режим"
        } else {
            "Перейти в позорный режим"
        }

        renderCube()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeView(R.layout.activity_cube)

        if (savedInstanceState != null) {
            currentProgress = savedInstanceState.getInt("currentProgress")
            isTerrible = savedInstanceState.getBoolean("isTerrible")
            dx = savedInstanceState.getFloat("dx")
            dy = savedInstanceState.getFloat("dy")
        }

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

        modeButton = findViewById(R.id.modeButton)
        modeButton.setOnClickListener { changeMode(!isTerrible) }
        changeMode(isTerrible)

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
                    if (event.pointerCount == 1) {
                        dx += (event.x - previousX) / 400
                        dy += (event.y - previousY) / 400

                        renderCube()

                        previousX = event.x
                        previousY = event.y
                    } else if (event.pointerCount == 2) {
                        val x1 = event.getX(0)
                        val x2 = event.getX(1)
                        val currentDistance = abs(x1 - x2)

                        if (currentDistance < startDistance && currentProgress < 100) {
                            currentProgress++
                            renderCube()
                        } else if (currentDistance > startDistance && currentProgress > 15) {
                            currentProgress--
                            renderCube()
                        }
                        startDistance = currentDistance
                    }
                }
            }

            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("currentProgress", currentProgress)
        outState.putBoolean("isTerrible", isTerrible)
        outState.putFloat("dx", dx)
        outState.putFloat("dy", dy)
    }
}