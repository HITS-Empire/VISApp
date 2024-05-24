package ru.tsu.visapp

import kotlin.math.sqrt
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.ImageView
import android.util.DisplayMetrics
import ru.tsu.visapp.utils.ImageEditor
import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable

/*
 * Экран для векторного редактора
 */

class VectorActivity : ChildActivity() {
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var imageEditor: ImageEditor

    private val cords = ArrayList<ArrayList<Int>>()
    private var k = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_vector)

        imageView = findViewById(R.id.splinesImage)
        imageEditor = ImageEditor()
        imageEditor.contentResolver = contentResolver

        // Размеры дисплея для более корректного отображения
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        bitmap = Bitmap.createBitmap(
            width,
            height - 100,
            Bitmap.Config.ARGB_8888
        )
        canvas = Canvas(bitmap)

        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 15.0f

        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Получение координат точки
                    val point = imageEditor.getPointFromImageView(
                        imageView,
                        event.x,
                        event.y,
                        bitmap.width,
                        bitmap.height
                    )

                    // Отрисовка точки
                    if (point != null) {
                        canvas.drawPoint(
                            point[0].toFloat(),
                            point[1].toFloat(),
                            paint
                        )
                        imageView.invalidate()
                    }

                    // Добавление координаты в список
                    cords.add(arrayListOf(point!![0], point[1]))
                    k += 1

                    // Отрисовка сплайнов
                    if (k > 3) drawSpline()
                }
            }

            true
        }

        // Установка отрисованного bitmap в ImageView
        imageView.background = BitmapDrawable(getResources(), bitmap)
    }

    private fun drawSpline() {
        var num = 0.0
        for (i in 0 until 2 * k step 2) {
            if (i > 0 && i < 2 * (k - 1)) {
                val deltaX = cords[i / 2 + 1][0] - cords[i / 2][0]
                val deltaY = cords[i / 2 + 1][1] - cords[i / 2][1]
                num += sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
            }
        }

        cords[0] = cords[1]
        cords.add(cords[cords.size - 1])

        // В цикле по всем четвёркам точек
        for (i in 1..cords.size - 3) {
            val a = mutableListOf(0.0, 0.0, 0.0, 0.0)
            val b = mutableListOf(0.0, 0.0, 0.0, 0.0)
            val arrs = mapOf("a" to a, "b" to b)

            // Считаем коэффициенты
            getSplineCoefficient(i, arrs)

            // Cоздаём массив промежуточных точек
            val points = mutableMapOf<String, Double>()

            for (j in 0 until num.toInt()) {
                // Шаг интерполяции
                val t = j.toDouble() / num

                val c = arrs["a"]!![1] + t * (arrs["a"]!![2] + t * arrs["a"]!![3])
                val d = arrs["b"]!![1] + t * (arrs["b"]!![2] + t * arrs["b"]!![3])

                // Передаём массиву точек значения по методу beta-spline
                points["X"] = arrs["a"]!![0] + t * c
                points["Y"] = arrs["b"]!![0] + t * d

                canvas.drawPoint(
                    points["X"]!!.toFloat(),
                    points["Y"]!!.toFloat(),
                    paint
                )
                imageView.invalidate()
            }
        }
    }

    private fun getSplineCoefficient(
        i: Int,
        arrs: Map<String, MutableList<Double>>
    ) {
        arrs["a"]!![3] =
            (-cords[i - 1][0] + 3.0 * cords[i][0] - 3.0 * cords[i + 1][0] + cords[i + 2][0]) / 6.0
        arrs["a"]!![2] =
            (cords[i - 1][0] - 2.0 * cords[i][0] + cords[i + 1][0]) / 2.0
        arrs["a"]!![1] =
            (-cords[i - 1][0] + cords[i + 1][0]) / 2.0
        arrs["a"]!![0] =
            (cords[i - 1][0] + 4.0 * cords[i][0] + cords[i + 1][0]) / 6.0
        arrs["b"]!![3] =
            (-cords[i - 1][1] + 3.0 * cords[i][1] - 3.0 * cords[i + 1][1] + cords[i + 2][1]) / 6.0
        arrs["b"]!![2] =
            (cords[i - 1][1] - 2.0 * cords[i][1] + cords[i + 1][1]) / 2.0
        arrs["b"]!![1] =
            (-cords[i - 1][1] + cords[i + 1][1]) / 2.0
        arrs["b"]!![0] =
            (cords[i - 1][1] + 4.0 * cords[i][1] + cords[i + 1][1]) / 6.0
    }
}