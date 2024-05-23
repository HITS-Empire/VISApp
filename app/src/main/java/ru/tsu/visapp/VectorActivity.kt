package ru.tsu.visapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.widget.ImageView
import ru.tsu.visapp.utils.ImageEditor

/*
 * Экран для векторного редактора
 */

class VectorActivity: ChildActivity() {
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var imageEditor: ImageEditor

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_vector)

        imageView = findViewById(R.id.splinesImage)
        imageEditor = ImageEditor()

        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)
        canvas = Canvas(bitmap)

        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 10F

        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("точка:")
                    println(event.x)
                    println(event.y)

                    val point = imageEditor.getPointFromImageView(
                        imageView,
                        event.x,
                        event.y,
                        bitmap.width,
                        bitmap.height
                    )
                    if (point != null) {
                        canvas.drawPoint(
                            point[0].toFloat(),
                            point[1].toFloat(),
                            paint
                        )
                        imageView.invalidate()
                    }
                }
            }

            true
        }

        // set bitmap as background to ImageView
        imageView.background = BitmapDrawable(getResources(), bitmap)
    }
}