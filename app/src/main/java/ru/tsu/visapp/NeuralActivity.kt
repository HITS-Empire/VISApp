package ru.tsu.visapp

import android.os.Bundle

/*
 * Экран для нейронной сети
 */

class NeuralActivity: ChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_neural)
    }
}