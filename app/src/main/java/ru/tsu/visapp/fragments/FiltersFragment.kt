package ru.tsu.visapp.fragments

import ru.tsu.visapp.R
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

class FiltersFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_filters, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(context, "Вы перешли на страницу фильтров", Toast.LENGTH_SHORT).show()
    }
}
