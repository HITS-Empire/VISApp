package ru.tsu.visapp.fragments

import ru.tsu.visapp.R
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.view.LayoutInflater
import ru.tsu.visapp.FiltersActivity
import androidx.fragment.app.Fragment
import ru.tsu.visapp.utils.ImageGetter

/*
 * Фрагмент "Главная"
 */

class HomeFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ImageGetter(null, this, goToFiltersActivity)
    }

    // Перейти на страницу фильтров
    private val goToFiltersActivity = fun() {
        val intent = Intent(requireContext(), FiltersActivity::class.java)
        startActivity(intent)
    }
}