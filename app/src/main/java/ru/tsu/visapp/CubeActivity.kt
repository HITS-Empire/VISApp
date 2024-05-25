package ru.tsu.visapp

import kotlin.math.*
import android.view.View
import android.os.Bundle
import android.widget.Button
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.ImageView
import ru.tsu.visapp.utils.cube.*
import android.widget.ImageButton
import android.graphics.BitmapFactory
import ru.tsu.visapp.utils.ImageEditor
import android.annotation.SuppressLint
import ru.tsu.visapp.utils.ImageGetter
import ru.tsu.visapp.utils.PixelsEditor

/*
 * Экран для 3D-куба
 */

class CubeActivity : ChildActivity() {
    private val width = 99 // Ширина картинки
    private val height = 99 // Высота картинки

    private var currentProgress = 50 // Текущий прогресс в процентах
    private var isTerrible = false // Включен ли режим профсоюза
    // Раньше был режим "позорного куба", переименовывать не стали...

    private lateinit var bitmap: Bitmap
    private lateinit var modeButton: Button
    private lateinit var imageView: ImageView
    private lateinit var imagePixels: Array<Pair<IntArray, Int>>
    private lateinit var initImagePixels: Array<Pair<IntArray, Int>>

    private val helper = Helper()
    private val imageEditor = ImageEditor()

    private var dx = 0.4f // Угол по X
    private var dy = 0.4f // Угол по Y

    // Кнопки галереи и камеры
    private lateinit var cameraButton: ImageButton
    private lateinit var galleryButton: ImageButton

    private fun renderCube() {
        val pixels = imageEditor.getPixelsFromBitmap(bitmap)
        val pixelsEditor = PixelsEditor(pixels, width, height)

        val boxSize = Vec3(1)
        val cameraPosition = Vec3(-currentProgress / 10.0f, 0.0f, 0.0f)
        cameraPosition.rotateY(dy)
        cameraPosition.rotateZ(dx)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val uv = Vec2(i, j) / Vec2(width, height) * 2.0f - 1.0f

                val beamDirection = Vec3(2, uv).normalize()
                beamDirection.rotateY(dy)
                beamDirection.rotateZ(dx)

                val color = helper.box(
                    boxSize,
                    cameraPosition,
                    beamDirection,
                    imagePixels,
                    width,
                    height,
                    cameraPosition,
                    isTerrible,
                )
                pixelsEditor.setPixel(i, j, color ?: 0)
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, pixels)
        imageView.setImageBitmap(bitmap)
    }

    private fun getPixelsFromDrawable(id: Int): Pair<IntArray, Int> {
        val options = BitmapFactory.Options()
        options.inScaled = false

        val imageBitmap = BitmapFactory.decodeResource(resources, id, options)
        val currentPixels = imageEditor.getPixelsFromBitmap(imageBitmap)

        return Pair(currentPixels, imageBitmap.width)
    }

    private fun changeMode(mode: Boolean) {
        isTerrible = mode

        if (isTerrible) {
            modeButton.text = "Уйти из профсоюза"
            galleryButton.visibility = View.VISIBLE
            cameraButton.visibility = View.VISIBLE
        } else {
            modeButton.text = "Вступить в профсоюз"
            galleryButton.visibility = View.INVISIBLE
            cameraButton.visibility = View.INVISIBLE
        }

        renderCube()
    }

    // Обработать картинку, загруженную пользователем
    private val processImage = fun() {
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        val imageBitmap = imageEditor.createBitmapByUri(savedImageUri)
        val newPixels = imageEditor.getPixelsFromBitmap(imageBitmap)

        for (i in initImagePixels.indices) {
            if (!initImagePixels[i].first.contentEquals(newPixels)) {
                initImagePixels[i] = Pair(newPixels, imageBitmap.width)
                imagePixels[i] = initImagePixels[i]
                break
            }
        }

        renderCube()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeView(R.layout.activity_cube)

        ImageGetter(this, null, processImage)

        if (savedInstanceState != null) {
            currentProgress = savedInstanceState.getInt("currentProgress")
            isTerrible = savedInstanceState.getBoolean("isTerrible")
            dx = savedInstanceState.getFloat("dx")
            dy = savedInstanceState.getFloat("dy")
        }

        imageEditor.contentResolver = contentResolver
        imageView = findViewById(R.id.cubeImageView)

        imagePixels = arrayOf(
            getPixelsFromDrawable(R.drawable.digit_1),
            getPixelsFromDrawable(R.drawable.digit_2),
            getPixelsFromDrawable(R.drawable.digit_3),
            getPixelsFromDrawable(R.drawable.digit_4),
            getPixelsFromDrawable(R.drawable.digit_5),
            getPixelsFromDrawable(R.drawable.digit_6)
        )

        initImagePixels = arrayOf(
            Pair(IntArray(1), 1),
            Pair(IntArray(1), 1),
            Pair(IntArray(1), 1),
            Pair(IntArray(1), 1),
            Pair(IntArray(1), 1),
            Pair(IntArray(1), 1)
        )

        for (i in imagePixels.indices) {
            initImagePixels[i] = imagePixels[i]
        }

        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        modeButton = findViewById(R.id.modeButton)
        modeButton.setOnClickListener { changeMode(!isTerrible) }
        changeMode(isTerrible)

        var previousX = 0.0f
        var previousY = 0.0f

        var startX1: Float
        var startX2: Float
        var startY1: Float
        var startY2: Float
        var startDistanceX = 0.0f
        var startDistanceY = 0.0f

        imageView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        startX1 = event.getX(0)
                        startX2 = event.getX(1)

                        startDistanceX = abs(startX1 - startX2)

                        startY1 = event.getX(0)
                        startY2 = event.getX(1)

                        startDistanceY = abs(startY1 - startY2)
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
                        val y1 = event.getY(0)
                        val y2 = event.getY(1)
                        val currentDistanceX = abs(x1 - x2)
                        val currentDistanceY = abs(y1 - y2)

                        if ((currentDistanceX < startDistanceX || currentDistanceY < startDistanceY) && currentProgress < 100) {
                            currentProgress++
                            renderCube()
                        } else if ((currentDistanceX > startDistanceX || currentDistanceY > startDistanceY) && currentProgress > 30) {
                            currentProgress--
                            renderCube()
                        }

                        startDistanceX = currentDistanceX
                        startDistanceY = currentDistanceY
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