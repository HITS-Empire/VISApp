package ru.tsu.visapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.widget.ImageView
import ru.tsu.visapp.utils.ImageEditor
import kotlin.math.sqrt


/*
 * Экран для векторного редактора
 */

class VectorActivity: ChildActivity() {
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var imageEditor: ImageEditor
    
    private val coords = ArrayList<ArrayList<Int>>()
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
        paint.strokeWidth = 15F

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
                    coords.add(arrayListOf(point!![0], point[1]))
                    k += 1

                    // Отрисовка сплайнов
                    if (k > 3) {
                        drawSpline();
                    }
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
                val deltaX = coords[i / 2 + 1][0] - coords[i / 2][0]
                val deltaY = coords[i / 2 + 1][1] - coords[i / 2][1]
                num += sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
            }
        }

        coords[0] = coords[1]
        coords.add(coords[coords.size - 1])

        // В цикле по всем четвёркам точек
        for (i in 1 .. coords.size - 3) {
            val a = mutableListOf(0.0, 0.0, 0.0, 0.0)
            val b = mutableListOf(0.0, 0.0, 0.0, 0.0)
            var arrs = mapOf("a" to a, "b" to b)

            // Считаем коэффициенты
            arrs = _SplineCoefficient(i, arrs)
            // Cоздаём массив промежуточных точек
            val points = mutableMapOf<String, Double>()

            for (j in 0 until num.toInt()) {
                // Шаг интерполяции
                val t = j.toDouble() / num

                // Передаём массиву точек значения по методу beta-spline
                points["X"] = (arrs["a"]!![0] + t * (arrs["a"]!![1] + t * (arrs["a"]!![2] + t * arrs["a"]!![3])))
                points["Y"] = (arrs["b"]!![0] + t * (arrs["b"]!![1] + t * (arrs["b"]!![2] + t * arrs["b"]!![3])))

                    canvas.drawPoint(
                        points["X"]!!.toFloat(),
                        points["Y"]!!.toFloat(),
                        paint
                    )
                    imageView.invalidate()
            }
        }
    }

    private fun _SplineCoefficient(
        i: Int,
        arrs: Map<String, MutableList<Double>>
    ): Map<String, MutableList<Double>> {
        var newArrs = arrs

        newArrs["a"]!![3] = (
                (-coords[i - 1][0] + 3*coords[i][0] - 3*coords[i + 1][0] + coords[i + 2][0]) / 6
                ).toDouble()
        newArrs["a"]!![2] = ((coords[i - 1][0] - 2*coords[i][0] + coords[i + 1][0])/2).toDouble()
        newArrs["a"]!![1] = ((-coords[i - 1][0] + coords[i + 1][0])/2).toDouble()
        newArrs["a"]!![0] = ((coords[i - 1][0] + 4*coords[i][0] + coords[i + 1][0])/6).toDouble()
        newArrs["b"]!![3] = ((-coords[i - 1][1] + 3*coords[i][1] - 3*coords[i + 1][1] + coords[i + 2][1])/6).toDouble()
        newArrs["b"]!![2] = ((coords[i - 1][1] - 2*coords[i][1] + coords[i + 1][1])/2).toDouble()
        newArrs["b"]!![1] = ((-coords[i - 1][1] + coords[i + 1][1])/2).toDouble()
        newArrs["b"]!![0] = ((coords[i - 1][1] + 4*coords[i][1] + coords[i + 1][1])/6).toDouble()

        return newArrs
    }
}