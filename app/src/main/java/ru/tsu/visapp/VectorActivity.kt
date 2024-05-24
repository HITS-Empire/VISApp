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
import kotlin.math.abs
import kotlin.math.pow

/*
 * Экран для векторного редактора
 */

class VectorActivity : ChildActivity() {
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var imageEditor: ImageEditor

    private val coordinates = ArrayList<ArrayList<Int>>()

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
        paint.color = Color.argb(255, 43, 203, 17)
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
                    coordinates.add(arrayListOf(point!![0], point[1]))

                    // Отрисовка сплайнов
                    if (coordinates.size > 3) {
                        drawSpline()
                    }
                }
            }

            true
        }

        // Установка отрисованного bitmap в ImageView
        imageView.background = BitmapDrawable(getResources(), bitmap)
    }

    // Антиалиасинг кривых
    private fun antiAliasing(
        path: ArrayList<ArrayList<Double>>
    ): ArrayList<ArrayList<Double>> {
        val newPath = ArrayList<ArrayList<Double>>()

        for (i in 0 until path.size - 1) {
            if (path[i].size == 2) {
                val x1 = path[i][0]
                val y1 = path[i][1]
                val x2 = path[i + 1][0]
                val y2 = path[i + 1][1]

                val dx = abs(x2 - x1)
                val dy = abs(y2 - y1)

                if (dx >= dy) {
                    val xStep = if (x1 < x2) 1 else -1
                    val yStep = dy / dx * xStep
                    var y = y1

                    if (x1 < x2) {
                        for (x in x1.toInt() until x2.toInt()) {
                            newPath.add(arrayListOf(x.toDouble(), y))
                            y += yStep
                        }
                    } else {
                        for (x in x1.toInt() downTo x2.toInt()) {
                            newPath.add(arrayListOf(x.toDouble(), y))
                            y += yStep
                        }
                    }
                } else {
                    val yStep = if (y1 < y2) 1 else -1
                    val xStep = dx / dy * yStep
                    var x = x1

                    if (y1 < y2) {
                        for (y in y1.toInt() until y2.toInt()) {
                            newPath.add(arrayListOf(x, y.toDouble()))
                            x += xStep
                        }
                    } else {
                        for (y in y1.toInt() downTo y2.toInt()) {
                            newPath.add(arrayListOf(x, y.toDouble()))
                            x += xStep
                        }
                    }
                }
            }
        }

        // Добавление последней точки
        newPath.add(path.last())

        return newPath
    }

    private fun getCoefficient(
        index: Int,
        arrs: MutableList<MutableList<Double>>
    ) {
        arrs[0][3] = (
                -coordinates[index - 1][0] +
                        3.0 * coordinates[index][0] -
                        3.0 * coordinates[index + 1][0] +
                        coordinates[index + 2][0]
                ) / 6.0
        arrs[0][2] = (
                coordinates[index - 1][0] -
                        2.0 * coordinates[index][0] +
                        coordinates[index + 1][0]
                ) / 2.0
        arrs[0][1] = (
                -coordinates[index - 1][0] +
                        coordinates[index + 1][0]
                ) / 2.0
        arrs[0][0] = (
                coordinates[index - 1][0] +
                        4.0 * coordinates[index][0] +
                        coordinates[index + 1][0]
                ) / 6.0
        arrs[1][3] = (
                -coordinates[index - 1][1] +
                        3.0 * coordinates[index][1] -
                        3.0 * coordinates[index + 1][1] +
                        coordinates[index + 2][1]
                ) / 6.0
        arrs[1][2] = (
                coordinates[index - 1][1] -
                        2.0 * coordinates[index][1] +
                        coordinates[index + 1][1]
                ) / 2.0
        arrs[1][1] = (
                -coordinates[index - 1][1] +
                        coordinates[index + 1][1]
                ) / 2.0
        arrs[1][0] = (
                coordinates[index - 1][1] +
                        4.0 * coordinates[index][1] +
                        coordinates[index + 1][1]
                ) / 6.0
    }

    private fun drawSpline() {
        var totalLength = 0.0
        for (i in 0 until 2 * coordinates.size step 2) {
            if (i > 0 && i < 2 * (coordinates.size - 1)) {
                val deltaX =
                    (coordinates[i / 2 + 1][0] - coordinates[i / 2][0]).toDouble()
                val deltaY =
                    (coordinates[i / 2 + 1][1] - coordinates[i / 2][1]).toDouble()
                totalLength += sqrt(deltaX.pow(2.0) + deltaY.pow(2.0))
            }
        }

        coordinates[0] = coordinates[1]
        coordinates.add(coordinates[coordinates.size - 1])

        // В цикле по всем четвёркам точек
        for (i in 1..coordinates.size - 3) {
            val currentPoints = mutableListOf(
                mutableListOf(0.0, 0.0, 0.0, 0.0),
                mutableListOf(0.0, 0.0, 0.0, 0.0)
            )

            // Считаем коэффициенты
            getCoefficient(i, currentPoints)

            // Массив точек сплайна
            val path: ArrayList<ArrayList<Double>> = arrayListOf(arrayListOf())

            for (j in 0 until totalLength.toInt()) {
                // Шаг интерполяции
                val step = j.toDouble() / totalLength

                val newPointX =
                    currentPoints[0][1] + step * (currentPoints[0][2] + step * currentPoints[0][3])
                val newPointY =
                    currentPoints[1][1] + step * (currentPoints[1][2] + step * currentPoints[1][3])

                // Считаем текущую точку
                val splinePoint = arrayListOf(
                    currentPoints[0][0] + step * newPointX,
                    currentPoints[1][0] + step * newPointY
                )

                path.add(ArrayList(splinePoint))
            }

            val newPath = antiAliasing(path)

            for (point in newPath) {
                if (point.size == 2) {
                    canvas.drawPoint(
                        point[0].toFloat(),
                        point[1].toFloat(),
                        paint
                    )
                    imageView.invalidate()
                }
            }
        }
    }
}