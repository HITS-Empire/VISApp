package ru.tsu.visapp.fragments

import ru.tsu.visapp.R
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.widget.ImageButton
import android.view.LayoutInflater
import ru.tsu.visapp.FiltersActivity
import androidx.fragment.app.Fragment
import ru.tsu.visapp.utils.ImageGetter
import android.content.res.Configuration
import android.view.animation.TranslateAnimation

/*
 * Фрагмент "Главная"
 */

class HomeFragment : Fragment() {
    // Кнопки на Главной
    private lateinit var plusButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var cameraButton: ImageButton

    // Выдвинуты ли сейчас кнопки
    private var isAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ImageGetter(null, this, goToFiltersActivity)

        plusButton = view.findViewById(R.id.plusButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        cameraButton = view.findViewById(R.id.cameraButton)

        plusButton.setOnClickListener { changeAvailable(!isAvailable) }

        changeAvailable(false)
    }

    // Изменить положение анимированных кнопок
    private fun changeAvailable(newAvailable: Boolean) {
        isAvailable = newAvailable

        val orientation = resources.configuration.orientation
        val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

        val visibility: Int
        val x1First: Float
        val x2First: Float
        val y1First: Float
        val y2First: Float
        val x1Second: Float
        val x2Second: Float
        val y1Second: Float
        val y2Second: Float

        if (isAvailable) {
            visibility = View.VISIBLE
            if (isLandscape) {
                x1First = 250.0f
                x2First = 0.0f
                y1First = 0.0f
                y2First = 0.0f
                x1Second = 450.0f
                x2Second = 0.0f
                y1Second = 0.0f
                y2Second = 0.0f
            } else {
                x1First = 0.0f
                x2First = 0.0f
                y1First = 250.0f
                y2First = 0.0f
                x1Second = 0.0f
                x2Second = 0.0f
                y1Second = 450.0f
                y2Second = 0.0f
            }
        } else {
            visibility = View.GONE
            if (isLandscape) {
                x1First = 0.0f
                x2First = 250.0f
                y1First = 0.0f
                y2First = 0.0f
                x1Second = 0.0f
                x2Second = 450.0f
                y1Second = 0.0f
                y2Second = 0.0f
            } else {
                x1First = 0.0f
                x2First = 0.0f
                y1First = 0.0f
                y2First = 250.0f
                x1Second = 0.0f
                x2Second = 0.0f
                y1Second = 0.0f
                y2Second = 450.0f
            }
        }

        galleryButton.visibility = visibility
        cameraButton.visibility = visibility

        animateView(galleryButton, x1First, x2First, y1First, y2First)
        animateView(cameraButton, x1Second, x2Second, y1Second, y2Second)
    }

    // Анимация передвижения для кнопки
    private fun animateView(
        view: View,
        x1: Float,
        x2: Float,
        y1: Float,
        y2: Float
    ) {
        val animation = TranslateAnimation(x1, x2, y1, y2)
        animation.duration = 400

        view.startAnimation(animation)
    }

    // Перейти на страницу фильтров
    private val goToFiltersActivity = fun() {
        val intent = Intent(requireContext(), FiltersActivity::class.java)
        startActivity(intent)
    }
}