package ru.tsu.visapp.filters

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.ImageView

@SuppressLint("ClickableViewAccessibility")
class Retouching(imageView: ImageView, pixels: IntArray, width: Int, height: Int,
                 retouchSize: Int, coefficient: Int) {
    private fun retouch() {

    }

    init {
        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("Еще чето")
                }

                MotionEvent.ACTION_MOVE -> {
                    println("Движение")
                }
            }

            true
        }
    }
}