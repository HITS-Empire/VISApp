package ru.tsu.visapp

import android.os.Bundle

/*
 * Экран для векторного редактора
 */

class VectorActivity: ChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_vector)
    }
}