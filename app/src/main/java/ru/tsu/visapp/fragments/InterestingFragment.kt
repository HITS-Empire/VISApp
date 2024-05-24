package ru.tsu.visapp.fragments

import ru.tsu.visapp.R
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import ru.tsu.visapp.CubeActivity
import android.view.LayoutInflater
import ru.tsu.visapp.NeuralActivity
import ru.tsu.visapp.VectorActivity
import androidx.fragment.app.Fragment

/*
 * Фрагмент "Интересное"
 */

class InterestingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_interesting, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Окошки алгоритмов
        val framesWithInteresting: Array<FrameLayout> = arrayOf(
            view.findViewById(R.id.neuralFrame),
            view.findViewById(R.id.vectorFrame),
            view.findViewById(R.id.cubeFrame)
        )

        // События кликов по фреймам
        framesWithInteresting.forEach { frame ->
            frame.setOnClickListener {
                val context = requireContext()
                var intent: Intent? = null

                when (frame.id) {
                    R.id.neuralFrame -> {
                        intent = Intent(context, NeuralActivity::class.java)
                    }

                    R.id.vectorFrame -> {
                        intent = Intent(context, VectorActivity::class.java)
                    }

                    R.id.cubeFrame -> {
                        intent = Intent(context, CubeActivity::class.java)
                    }
                }

                startActivity(intent)
            }
        }
    }
}