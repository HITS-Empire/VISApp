package ru.tsu.visapp

import android.os.Bundle

/*
 * Экран для 3D-куба
 */

class CubeActivity: ChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_cube)
    }
}